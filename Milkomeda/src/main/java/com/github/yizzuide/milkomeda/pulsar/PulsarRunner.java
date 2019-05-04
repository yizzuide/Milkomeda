package com.github.yizzuide.milkomeda.pulsar;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.concurrent.Callable;

/**
 * PulsarRunner
 * 基于Runnable的装饰运行器，可自动捕获 Throwable，并给出相应的错误反馈
 *
 * @since 1.1.0
 * @author yizzuide
 * Create at 2019/05/03 23:53
 */
@Slf4j
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
        this.callable = callable;
        this.deferredResult = pulsarDeferredResult.getDeferredResult();
    }

    public PulsarRunner(Callable<Object> callable, String deferredResultID) {
        this(callable, PulsarHolder.getPulsar().takePulsarDeferredResult(deferredResultID));
    }

    /**
     * 覆盖线程调度运行的 run()方法，使用装饰模式增强来支持统一捕获Throwable异常
     */
    @Override
    public void run() {
        try {
            Object data = callable.call();
            if (null != data) {
                // 如果有返回值，使用DeferredResult返回
                deferredResult.setResult(data);
                return;
            }
            // 如果返回为 null, 响应200
            deferredResult.setResult(ResponseEntity.status(HttpStatus.OK).build());
        } catch (Exception e) {
            log.error("pulsar:- PulsarRunner catch a error with message: {} ", e.getMessage(), e);
            if (null != PulsarHolder.getErrorCallback()) {
                deferredResult.setErrorResult(PulsarHolder.getErrorCallback().apply(e));
            }
        }
    }
}
