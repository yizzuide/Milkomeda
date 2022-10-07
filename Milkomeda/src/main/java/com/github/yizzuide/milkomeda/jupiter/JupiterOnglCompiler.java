/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.jupiter;

import com.github.yizzuide.milkomeda.universe.engine.ognl.OgnlClassResolver;
import com.github.yizzuide.milkomeda.universe.engine.ognl.OgnlMemberAccess;
import lombok.extern.slf4j.Slf4j;
import ognl.ClassResolver;
import ognl.MemberAccess;
import ognl.Ognl;
import ognl.OgnlException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JupiterOnglCompiler
 *
 * @author yizzuide
 * @since 3.5.0
 * <br />
 * Create at 2020/05/20 11:39
 */
@Slf4j
public class JupiterOnglCompiler implements JupiterExpressionCompiler {
    private final MemberAccess MEMBER_ACCESS = new OgnlMemberAccess(false);
    private final ClassResolver CLASS_RESOLVER = new OgnlClassResolver();
    private final Map<String, Object> expressionCache = new ConcurrentHashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T> T compile(String expression, Object root, Class<T> resultType) {
        try {
            Map ognlContext = Ognl.createDefaultContext(root, MEMBER_ACCESS, CLASS_RESOLVER, null);
            return (T) Ognl.getValue(parseExpression(expression), ognlContext, root, resultType);
        } catch (OgnlException e) {
            log.error("Jupiter ongl compiler error with msg: {}", e.getMessage(), e);
        }
        return null;
    }

    // 缓存表达式解析抽象树对象
    private Object parseExpression(String expression) throws OgnlException {
        Object node = expressionCache.get(expression);
        if (node == null) {
            node = Ognl.parseExpression(expression);
            expressionCache.put(expression, node);
        }
        return node;
    }
}
