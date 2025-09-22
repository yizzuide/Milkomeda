/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.quark;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;

import java.util.*;

/**
 * A helper class to make chain with event handlers.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2024/06/15 20:57
 */
public class QuarkChainASTHelper {

    private static Map<String, List<QuarkEventHandler<?>>> topicEventHandlerMap;

    /* topic -> AST 根节点 */
    private static final Map<String, Node> topicAstCache = new HashMap<>();

    @SuppressWarnings({"unchecked"})
    public static List<QuarkEventHandler<?>> invoke(Disruptor<QuarkEvent<Object>> disruptor, String topic) {
        List<QuarkEventHandler<?>> handlers = topicEventHandlerMap.get(topic);
        if (disruptor == null) {          // 只查配置，不绑定
            return handlers;
        }
        Node ast = topicAstCache.get(topic);
        if (ast == null) {                // 没有配置链式表达式，退化为并发执行
            disruptor.handleEventsWith(handlers.toArray(new EventHandler[0]));
            return handlers;
        }
        // 真正绑定 Disruptor
        buildDisruptor(disruptor, ast);
        return handlers;
    }

    public static void setTopicEventHandlers(Map<String, List<QuarkEventHandler<?>>> topicEventHandlerMap,
                                             Map<String, QuarkEventHandler<?>> namedEventHandlerMap,
                                             Map<String, String> chainMap) {
        QuarkChainASTHelper.topicEventHandlerMap = topicEventHandlerMap;
        chainMap.forEach((topic, expr) -> {
            if (expr == null || expr.isBlank()) {
                return;
            }
            Node ast = parse(expr, namedEventHandlerMap);
            topicAstCache.put(topic, ast);
        });
    }

    /* ---------- 1. AST 定义 ---------- */
    private sealed interface Node {
        /** 构建并返回本节点的 group */
        EventHandlerGroup<?> build(Disruptor<QuarkEvent<Object>> d, EventHandlerGroup<?> parent);

        /** 返回本节点产生的**所有**需要被等待的 EventHandler（叶子） */
        EventHandler<?>[] lastHandlers();
    }

    /* ---------- 1.2. HandlerNode ---------- */
    private record HandlerNode(QuarkEventHandler<?> handler) implements Node {
        @SuppressWarnings("rawtypes")
        @Override
        public EventHandlerGroup<?> build(Disruptor<QuarkEvent<Object>> d, EventHandlerGroup<?> parent) {
            return parent == null
                    ? d.handleEventsWith((EventHandler) handler)
                    : parent.handleEventsWith((EventHandler) handler);
        }
        @Override
        public EventHandler<?>[] lastHandlers() {
            return new EventHandler[]{handler};
        }
    }

    /* ---------- 1.3. SequentialNode ---------- */
    private record SequentialNode(List<Node> children) implements Node {
        @Override
        public EventHandlerGroup<?> build(Disruptor<QuarkEvent<Object>> d, EventHandlerGroup<?> parent) {
            EventHandlerGroup<?> cur = parent;
            for (Node c : children) {
                cur = c.build(d, cur);
            }
            return cur;
        }
        @Override
        public EventHandler<?>[] lastHandlers() {
            // 串行节点只拿最后一个孩子的叶子
            return children.getLast().lastHandlers();
        }
    }

    /* ---------- 1.4. ParallelNode ---------- */
    private record ParallelNode(List<Node> children) implements Node {
        @SuppressWarnings("unchecked")
        @Override
        public EventHandlerGroup<?> build(Disruptor<QuarkEvent<Object>> d,
                                          EventHandlerGroup<?> parent) {
            /* 1. 先把每个子流程挂在 parent 后面（保证它们已经串好了） */
            for (Node c : children) {
                c.build(d, parent);
            }

            /* 2. 收集所有叶子 handler */
            List<EventHandler<?>> leaves = new ArrayList<>();
            for (Node c : children) {
                Collections.addAll(leaves, c.lastHandlers());
            }

            /* 3. 用 Disruptor 做并行汇合 */
            return (EventHandlerGroup<?>) d.after(leaves.toArray(new EventHandler[0]))
                    .handleEventsWith(NoOpHandler.INSTANCE);
        }

        @Override
        public EventHandler<?>[] lastHandlers() {
            return new EventHandler[]{NoOpHandler.INSTANCE};
        }
    }

    /* ---------- 1.5. 空 handler 保持不变 ---------- */
    enum NoOpHandler implements EventHandler<QuarkEvent<?>> {
        INSTANCE;
        @Override public void onEvent(QuarkEvent<?> event, long sequence, boolean endOfBatch) { }
    }

    /* ---------- 2. 解析器 ---------- */
    private static Node parse(String expr, Map<String, QuarkEventHandler<?>> named) {
        return new Parser(expr, named).parse();
    }

    private static class Parser {
        private final String expr;
        private final Map<String, QuarkEventHandler<?>> named;
        private int pos = 0;

        Parser(String expr, Map<String, QuarkEventHandler<?>> named) {
            this.expr = expr.replaceAll("\\s+", ""); // 去空白
            this.named = named;
        }

        Node parse() {
            Node node = parseSequential();
            if (pos != expr.length()) {
                throw new IllegalArgumentException("多余字符:" + expr.substring(pos));
            }
            return node;
        }

        /* 顶级：h -> xxx */
        private Node parseSequential() {
            List<Node> seq = new ArrayList<>();
            seq.add(parseParallel());
            while (peek() == '-' && look(1) == '>') {
                consume2(); // 跳过 ->
                seq.add(parseParallel());
            }
            return seq.size() == 1 ? seq.getFirst() : new SequentialNode(seq);
        }

        /* 并行：h,h,h */
        private Node parseParallel() {
            List<Node> par = new ArrayList<>();
            par.add(parsePrimary());
            while (peek() == ',') {
                consume(); // 跳过 ,
                par.add(parsePrimary());
            }
            return par.size() == 1 ? par.getFirst() : new ParallelNode(par);
        }

        /* 原子：name 或 ( Sequential ) */
        private Node parsePrimary() {
            if (peek() == '(') {
                consume(); // (
                Node inside = parseSequential();
                if (consume() != ')') throw new IllegalArgumentException("缺')'");
                return inside;
            }
            // 读名字
            int start = pos;
            while (pos < expr.length() && isIdChar(expr.charAt(pos))) pos++;
            String name = expr.substring(start, pos);
            QuarkEventHandler<?> h = named.get(name);
            if (h == null) throw new IllegalArgumentException("未注册 handler:" + name);
            return new HandlerNode(h);
        }

        private char peek() {
            return pos < expr.length() ? expr.charAt(pos) : '\0';
        }

        private char look(int delta) {
            int p = pos + delta;
            return p < expr.length() ? expr.charAt(p) : '\0';
        }

        private char consume() {
            return expr.charAt(pos++);
        }

        private void consume2() {
            pos += 2;
        }

        private boolean isIdChar(char c) {
            return Character.isJavaIdentifierPart(c);
        }
    }

    /* ---------- 3. 真正绑定 Disruptor ---------- */
    private static void buildDisruptor(Disruptor<QuarkEvent<Object>> disruptor, Node ast) {
        ast.build(disruptor, null);
    }
}
