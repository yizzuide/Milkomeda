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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * PulsarRunner
 * 基于Runnable的装饰运行器，可自动捕获 Throwable，并给出相应的错误反馈
 *
 * @since 1.1.0
 * @version 1.16.0
 * @author yizzuide
 * Create at 2019/05/03 23:53
 */
@Slf4j
@AllArgsConstructor
public class PulsarRunner implements Runnable {
    /**
     * 被装饰的接口
     */
    private Callable<Object> callable;

    /**
     * 每个PulsarRunner有一个DeferredResult，用于正常数据响应和发出异常时反馈
     */
    private DeferredResult<Object> deferredResult;

    /**
     * 对线程调度运行方法增强，使用装饰模式来支持统一捕获异常
     */
    @Override
    public void run() {
        try {
            Object value = callable.call();
            deferredResult.setResult(Optional.ofNullable(value)
                    .orElse(ResponseEntity.status(HttpStatus.OK).build()));
        } catch (Exception e) {
            log.error("pulsar:- PulsarRunner catch a error with message: {} ", e.getMessage(), e);
            if (null != deferredResult && null != PulsarHolder.getErrorCallback()) {
                deferredResult.setErrorResult(PulsarHolder.getErrorCallback().apply(e));
            }
        }
    }
}
