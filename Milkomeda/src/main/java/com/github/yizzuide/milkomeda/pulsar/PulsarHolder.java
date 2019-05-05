package com.github.yizzuide.milkomeda.pulsar;

import java.util.concurrent.Callable;
import java.util.function.Function;

/**
 * PulsarHolder
 * Pulsar 静态资源引用类
 *
 * @author yizzuide
 * @since 1.0.0
 * @version 1.2.1
 * Create at 2019/04/30 15:47
 */
public class PulsarHolder {

    private static Function<Throwable, Object> errorCallback;

    private static Pulsar pulsar;

    static void setErrorCallback(Function<Throwable, Object> errorCallback) {
        PulsarHolder.errorCallback = errorCallback;
    }

    static void setPulsar(Pulsar pulsar) { PulsarHolder.pulsar = pulsar; }

    /**
     * 可抛出异常回调，外部可直接调用来触发设置的回调执行
     */
    public static Function<Throwable, Object> getErrorCallback() {
        return errorCallback;
    }

    /**
     * 获取Pulsar
     * @return Pulsar
     */
    public static Pulsar getPulsar() {
        return pulsar;
    }

    /**
     * 通过自定义的 PulsarRunner 装饰器异步运行耗时请求处理
     *
     * @param pulsarRunner 异步装饰运行器
     * @return 配合和 @PulsarAsync 的使用（异步可以直接返回 null），其它地方忽略这个返回值
     */
    public static Object async(PulsarRunner pulsarRunner) {
        pulsar.asyncRun(pulsarRunner);
        return null;
    }

    /**
     * 通过 Callable 和 PulsarDeferredResult 异步运行耗时请求处理
     *
     * @param callable 异步运行方法，业务代码里可以直接返回数据，由框架异步来接管返回。如：return ResponseEntity.ok(data);
     * @param pulsarDeferredResult  基于Pulsar包装的DeferredResult
     * @return 配合和 @PulsarAsync 的使用（异步可以直接返回 null），其它地方忽略这个返回值
     */
    public static Object async(Callable<Object> callable, PulsarDeferredResult pulsarDeferredResult) {
         pulsar.asyncRun(new PulsarRunner(callable, pulsarDeferredResult));
         return null;
    }
}
