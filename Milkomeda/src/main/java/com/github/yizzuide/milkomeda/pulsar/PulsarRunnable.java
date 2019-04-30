package com.github.yizzuide.milkomeda.pulsar;

import lombok.AllArgsConstructor;

/**
 * PulsarRunnable
 * Pulsar 可运行抽象类
 *
 * @author yizzuide
 * @since 1.0.0
 * @version 1.0.0
 * Create at 2019/04/30 16:41
 */
@AllArgsConstructor
public abstract class PulsarRunnable implements Runnable {
    /**
     * 第个PulsarRunnable必须有一个DeferredResult，用于发出异常时可以响应，而不是等待超时
     */
    private PulsarDeferredResult pulsarDeferredResult;

    /**
     * 覆盖线程调度运行的 run()方法，使用装饰模式增强来支持统一捕获可抛出异常
     */
    @Override
    public void run() {
        try {
            runFlow(pulsarDeferredResult);
        } catch (Throwable t) {
            if (null == PulsarHolder.getErrorCallback()) {
                t.printStackTrace();
                return;
            }
            pulsarDeferredResult.take().setErrorResult(PulsarHolder.getErrorCallback().apply(t));
        }
    }

    /**
     * 调用方具体执行方法
     * @throws Throwable 可抛出异常
     */
    protected abstract void runFlow(PulsarDeferredResult pulsarDeferredResult) throws Throwable;
}
