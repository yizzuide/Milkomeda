/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.demo.orbit;

import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import com.github.yizzuide.milkomeda.orbit.orbit.OrbitAround;
import com.github.yizzuide.milkomeda.orbit.orbit.OrbitHandler;
import com.github.yizzuide.milkomeda.orbit.orbit.OrbitHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * OrderAdviceHandler
 *
 * @author yizzuide
 * Create at 2025/05/18 17:15
 */
@Slf4j
@OrbitHandler
public class OrderAdviceHandler {
    @OrbitAround
    public Object pushOrder(String orderNo/*, OrbitInvocation invocation*/) { // 获取OrbitInvocation方式一：在方法参数上注入
        log.info("订单推送前置处理：{}", orderNo);
        // 获取OrbitInvocation方式二：通过上下文获取（推荐，可以保持方法参数与被代理方法参数一致）
        OrbitInvocation invocation = OrbitHandlerContext.getInvocation();
        // 获取原目标对象，用于直接调用（防止循环切面处理）
        //OrderAPI orderAPI = invocation.getTarget(OrderAPI.class);
        return invocation.proceed();
    }
}
