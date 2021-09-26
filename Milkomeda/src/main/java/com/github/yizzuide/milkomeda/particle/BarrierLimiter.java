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

package com.github.yizzuide.milkomeda.particle;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * BarrierLimiter
 * 组合限制器
 *
 * 用于组装多个限制处理器，也能实现复合链串：限制器 + 组合限制器 + ...
 *
 * @author yizzuide
 * @since 1.5.0
 * @version 3.0.0
 * Create at 2019/05/31 11:25
 */
public class BarrierLimiter extends LimitHandler {
    /**
     * 拦截链头
     */
    private LimitHandler head;

    /**
     * 限制器名链（作为YML配置使用）
     */
    @Setter @Getter
    private List<String> chain;

    /**
     * 添加限制处理器
     * @param limitHandlerList 限制处理器集合
     */
    public void addLimitHandlerList(List<LimitHandler> limitHandlerList) {
        for (LimitHandler handler : limitHandlerList) {
            if (head == null) {
                head = handler;
                next = head;
                continue;
            }
            next.setNext(handler);
            next = next.getNext();
        }
    }

    @Override
    public <R> R limit(String key, long expire, Process<R> process) throws Throwable {
        return head.limit(key, expire, process);
    }
}
