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
 * @version 1.2.0
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

    public PulsarRunner(Callable<Object> callable, PulsarDeferredResult pulsarDeferredResult) {
        this(callable, pulsarDeferredResult == null ? null : pulsarDeferredResult.take());
    }

    public PulsarRunner(Callable<Object> callable, String deferredResultID) {
        this(callable, PulsarHolder.getPulsar().takePulsarDeferredResult(deferredResultID));
        if (null == this.deferredResult) {
            log.error("Pulsar:- Can't find deferredResultID \"{}\" in container, that maybe cause NullPointerException.", deferredResultID);
        }
    }

    /**
     * 覆盖线程调度运行的 run()方法，使用装饰模式增强来支持统一捕获Throwable异常
     */
    @Override
    public void run() {
        try {
            Object value = callable.call();
            deferredResult.setResult(Optional.of(value)
                    .orElse(ResponseEntity.status(HttpStatus.OK).build()));
        } catch (Exception e) {
            log.error("pulsar:- PulsarRunner catch a error with message: {} ", e.getMessage(), e);
            if (null != deferredResult && null != PulsarHolder.getErrorCallback()) {
                deferredResult.setErrorResult(PulsarHolder.getErrorCallback().apply(e));
            }
        }
    }
}
