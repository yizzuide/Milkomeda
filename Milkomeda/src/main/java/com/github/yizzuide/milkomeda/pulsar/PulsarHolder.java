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

package com.github.yizzuide.milkomeda.pulsar;

import io.netty.util.HashedWheelTimer;
import lombok.Getter;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.Callable;

/**
 * Pulsar 引用类
 *
 * @since 1.0.0
 * @version 4.0.0
 * @author yizzuide
 * <br>
 * Create at 2019/04/30 15:47
 */
public class PulsarHolder {

    @Getter
    private static Pulsar pulsar;

    // Netty 时间轮
    static HashedWheelTimer hashedWheelTimer;

    static void setPulsar(Pulsar pulsar) { PulsarHolder.pulsar = pulsar; }

    static void setHashedWheelTimer(HashedWheelTimer hashedWheelTimer) {
        PulsarHolder.hashedWheelTimer = hashedWheelTimer;
    }

    /**
     * 通过 Callable 和 PulsarDeferredResult 推迟运行耗时请求处理再返回
     *
     * @param callable  运行方法，业务代码里可以直接返回数据。如：return ResponseEntity.ok(data);
     * @param identifier    PulsarDeferredResult或PulsarDeferredResultID
     * @return 返回null用于配合 @PulsarFlow 的使用，其它地方使用可以忽略这个返回值（因为这个不是真实要返回的数据）
     */
    public static Object defer(Callable<Object> callable, Object identifier) {
        DeferredResult<Object> deferredResult;
        if (identifier instanceof String || identifier instanceof Integer || identifier instanceof Long) {
            deferredResult = pulsar.getDeferredResult(String.valueOf(identifier));
        } else if (identifier instanceof PulsarDeferredResult) {
            deferredResult = ((PulsarDeferredResult) identifier).getDeferredResult();
        } else {
            throw new IllegalArgumentException("identifier " + identifier + " is invalid.");
        }
        pulsar.post(new PulsarRunner(callable, deferredResult));
        return null;
    }
}
