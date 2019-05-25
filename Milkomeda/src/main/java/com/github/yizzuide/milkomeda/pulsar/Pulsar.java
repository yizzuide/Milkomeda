package com.github.yizzuide.milkomeda.pulsar;

import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

/**
 * Pulsar
 *
 * 用于存放和管理DeferredResult的容器，配合应用注解 <code>@PulsarFlow</code> 切面，
 * 并提供参数注入DeferredResult的包装类 <code>PulsarDeferredResult</code> 到请求参数列表。
 *
 * @author yizzuide
 * @since  0.1.0
 * @version 1.4.0
 * Create at 2019/03/29 10:36
 */
@Slf4j
@Aspect
public class Pulsar {
    /**
     * DeferredResult容器
     */
    private Map<String, PulsarDeferredResult> deferredResultMap;

    /**
     * 线程池执行器
     */
    @Autowired @Qualifier("pulsarTaskExecutor")
    private ThreadPoolTaskExecutor taskExecutor;

    /**
     *  Error 错误回调
     *
     *  Throwable：错误异常
     *  Object：响应数据
     */
    private Function<Throwable, Object> errorCallback;

    /**
     * 超时回调，返回参数为自定义响应数据
     */
    private Callable<Object> timeoutCallback;

    /**
     * 初始化容器容量大小
     */
    private static final int DEFAULT_CAPACITY = 64;

    public Pulsar() {
        deferredResultMap = new ConcurrentHashMap<>(DEFAULT_CAPACITY);
        PulsarHolder.setPulsar(this);
    }

    /**
     * 记存一个DeferredResult
     * @param pulsarDeferredResult PulsarDeferredResult
     */
    private void putDeferredResult(PulsarDeferredResult pulsarDeferredResult) {
        deferredResultMap.put(pulsarDeferredResult.getDeferredResultID(), pulsarDeferredResult);
    }

    /**
     * 通过标识符取出PulsarDeferredResult
     * @param id 标识符
     * @return PulsarDeferredResult
     */
    PulsarDeferredResult getPulsarDeferredResult(String id) {
        return deferredResultMap.get(id);
    }

    /**
     * 通过标识符得到DeferredResult
     * @param id 标识符
     * @return DeferredResult
     */
    public DeferredResult<Object> getDeferredResult(String id) {
        PulsarDeferredResult pulsarDeferredResult = getPulsarDeferredResult(id);
        return pulsarDeferredResult == null ? null : pulsarDeferredResult.getDeferredResult();
    }

    /**
     * 移除DeferredResult
     * @param id 标识符
     */
    private void removeDeferredResult(String id) {
        deferredResultMap.remove(id);
    }

    /**
     * 对使用了 @PulsarFlow 注解实现环绕切面
     * @param joinPoint 切面连接点
     * @return 响应数据对象
     * @throws Throwable 可抛出异常
     */
    @Around("@annotation(com.github.yizzuide.milkomeda.pulsar.PulsarFlow)")
    Object handlePulse(ProceedingJoinPoint joinPoint) throws Throwable {
        // 检测方法返回值
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String invokeMethodName = joinPoint.getSignature().getName();
        if (methodSignature.getReturnType() != Object.class) {
            throw new ClassCastException("you must set [Object] return type on method " +
                    invokeMethodName);
        }

        // 获取注解信息
        PulsarFlow pulsarFlow = ReflectUtil.getAnnotation(joinPoint, PulsarFlow.class);

        // 如果没有设置DeferredResult，则使用WebAsyncTask
        if (!pulsarFlow.useDeferredResult()) {
            // 返回异步任务
            WebAsyncTask<Object> webAsyncTask = new WebAsyncTask<>(new WebAsyncTaskCallable(joinPoint));
            if (null != timeoutCallback) {
                webAsyncTask.onTimeout(timeoutCallback);
            }
            return webAsyncTask;
        }

        // 使用DeferredResult方式
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        if (null != timeoutCallback) {
            // 适配超时处理
            deferredResult.onTimeout(() -> {
                try {
                    log.warn("pulsar:- DeferredResult handle timeout on method {}", invokeMethodName);
                    deferredResult.setErrorResult(timeoutCallback.call());
                } catch (Exception e) {
                    log.error("pulsar:- DeferredResult happen timeout callback error with message: {} ", e.getMessage(), e);
                    if (null == this.errorCallback) return;
                    this.errorCallback.apply(e);
                }
            });
        }

        if (null != errorCallback) {
            // 适配错误处理
            deferredResult.onError((throwable) -> deferredResult.setErrorResult(errorCallback.apply(throwable)));
        }

        // 创建增强DeferredResult
        PulsarDeferredResult pulsarDeferredResult = new PulsarDeferredResult();
        pulsarDeferredResult.setDeferredResult(deferredResult);

        // 调用方法实现
        Object returnObj = joinPoint.proceed(injectDeferredResult(joinPoint, pulsarDeferredResult,
                pulsarFlow.useDeferredResult()));

        // 方法有返回值且不是DeferredResult，则不作DeferredResult处理
        if (null != returnObj && !(returnObj instanceof DeferredResult)) {
            return returnObj;
        }

        // 检查是否设置标识
        if (null == pulsarDeferredResult.getDeferredResultID()) {
            throw new IllegalArgumentException("you must invoke setDeferredResultID method of PulsarDeferredResult parameter on method " +
                    invokeMethodName);
        }

        // 无论超时还是成功响应，删除这个DeferredResult
        deferredResult.onCompletion(() -> removeDeferredResult(pulsarDeferredResult.getDeferredResultID()));

        // 放入容器
        putDeferredResult(pulsarDeferredResult);
        // 返回
        return deferredResult;
    }

    /**
     * 基于WebAsyncTask实现的Callable
     */
    private class WebAsyncTaskCallable implements Callable<Object> {
        /**
         * 切面连接点
         */
        private ProceedingJoinPoint joinPoint;

        WebAsyncTaskCallable(ProceedingJoinPoint joinPoint) {
            this.joinPoint = joinPoint;
        }

        @Override
        public Object call() throws Exception {
            log.debug("pulsar:- WebAsyncTask invoke method: {}", joinPoint.getSignature());
            try {
                return joinPoint.proceed();
            } catch (Throwable t) {
                log.error("pulsar:-  WebAsyncTask invoke error with message: {}", t.getMessage(), t);
                // 如果有Exception异常向外抛，交由开发者处理
                if (t instanceof Exception) {
                    throw (Exception) t;
                } else { // Error类型
                    if (null != errorCallback) {
                        return errorCallback.apply(t);
                    }
                    return ResponseEntity.status(500).body(t.getMessage());
                }
            }
        }
    }

    /**
     * 异步运行
     * @param runnable Runnable
     */
    public void asyncRun(Runnable runnable) {
        taskExecutor.execute(runnable);
    }

    /**
     * 配置默认的异步支持
     * @param configurer 配置对象
     * @param timeout 超时时间，ms
     */
    public void configure(AsyncSupportConfigurer configurer, long timeout) {
        configure(configurer, 5, 10, 50, 200, timeout);
    }

    /**
     * 自定义配置的异步支持
     * @param configurer        配置对象
     * @param corePoolSize      核心池大小
     * @param maxPoolSize       最大线程池数
     * @param queueCapacity     队列容量
     * @param keepAliveSeconds  线程保存存活时间
     * @param timeout           超时时间，ms
     */
    public void configure(AsyncSupportConfigurer configurer, int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds, long timeout) {
        // 默认超时时间
        configurer.setDefaultTimeout(timeout);
        // 线程池维护线程的最少数量
        taskExecutor.setCorePoolSize(corePoolSize);
        // 线程池维护线程的最大数量
        taskExecutor.setMaxPoolSize(maxPoolSize);
        // 线程池所使用的缓冲队列
        taskExecutor.setQueueCapacity(queueCapacity);
        // 线程池维护线程所允许的空闲时间
        taskExecutor.setKeepAliveSeconds(keepAliveSeconds);
        taskExecutor.setThreadNamePrefix("pulsar-");
        // 线程池对拒绝任务（无线程可用）的处理策略，目前只支持AbortPolicy、CallerRunsPolicy，默认为后者
        taskExecutor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        taskExecutor.initialize();
        configurer.setTaskExecutor(taskExecutor);
    }

    /**
     * 用于处理 Error 类型（Exception类型还是使用 @ExceptionHandler 捕获）
     * @param errorCallback 失败回调
     */
    public void setErrorCallback(Function<Throwable, Object> errorCallback) {
        this.errorCallback = errorCallback;
        PulsarHolder.setErrorCallback(this.errorCallback);
    }

    /**
     * 处理超时
     * @param timeoutCallback 需要返回响应的超时数据
     */
    public void setTimeoutCallback(Callable<Object> timeoutCallback) {
        this.timeoutCallback = timeoutCallback;
    }

    /**
     * 注入DeferredResult
     * @param joinPoint 切面连接点
     * @param deferredResult DeferredResult标识装配类
     * @param check 是否检查添加了<code>PulsarDeferredResult</code>类型参数
     * @return 注入完成的参数
     */
    private Object[] injectDeferredResult(JoinPoint joinPoint, PulsarDeferredResult deferredResult, boolean check) {
        Object[] args = joinPoint.getArgs();
        int len = args.length;
        boolean flag = false;
        for (int i = 0; i < len; i++) {
            if (args[i] instanceof PulsarDeferredResult) {
                args[i] = deferredResult;
                flag = true;
                return args;
            }
        }
        if (check && !flag) {
            throw new IllegalArgumentException("you must add PulsarDeferredResult parameter on method " +
                    joinPoint.getSignature().getName() + " and set deferredResultID before use DeferredResult.");
        }
        return args;
    }
}
