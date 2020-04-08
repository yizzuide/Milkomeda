package com.github.yizzuide.milkomeda.pulsar;

import com.github.yizzuide.milkomeda.util.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static com.github.yizzuide.milkomeda.util.ReflectUtil.*;

/**
 * Pulsar
 * <p>
 * 用于存放和管理DeferredResult的容器，配合应用注解 <code>@PulsarFlow</code> 切面，
 * 并提供参数注入DeferredResult的包装类 <code>PulsarDeferredResult</code> 到请求参数列表。
 *
 * @author yizzuide
 * @since 0.1.0
 * @version 1.16.0
 * Create at 2019/03/29 10:36
 */
@Slf4j
@Aspect
@Order(66)
public class Pulsar {
    /**
     * DeferredResult容器
     */
    private Map<String, PulsarDeferredResult> deferredResultMap;

    /**
     * 线程池执行器（从SpringBoot 2.1.0开始默认已经装配）
     */
    @Autowired
    private ThreadPoolTaskExecutor applicationTaskExecutor;

    /**
     * 超时回调，返回参数为自定义响应数据
     */
    private Supplier<Object> timeoutCallback;

    /**
     * 初始化容器容量大小
     */
    private static final int DEFAULT_CAPACITY = 64;

    Pulsar() {
        deferredResultMap = new ConcurrentHashMap<>(DEFAULT_CAPACITY);
        PulsarHolder.setPulsar(this);
    }

    /**
     * 记存一个DeferredResult
     *
     * @param pulsarDeferredResult PulsarDeferredResult
     */
    private void putDeferredResult(PulsarDeferredResult pulsarDeferredResult) {
        deferredResultMap.put(pulsarDeferredResult.getDeferredResultID(), pulsarDeferredResult);
    }

    /**
     * 通过标识符取出PulsarDeferredResult
     *
     * @param id 标识符
     * @return PulsarDeferredResult
     */
    PulsarDeferredResult getPulsarDeferredResult(String id) {
        return deferredResultMap.get(id);
    }

    /**
     * 通过标识符得到DeferredResult
     *
     * @param id 标识符
     * @return DeferredResult
     */
    public DeferredResult<Object> getDeferredResult(String id) {
        PulsarDeferredResult pulsarDeferredResult = getPulsarDeferredResult(id);
        return pulsarDeferredResult == null ? null : pulsarDeferredResult.getDeferredResult();
    }

    /**
     * 移除DeferredResult
     *
     * @param id 标识符
     */
    private void removeDeferredResult(String id) {
        deferredResultMap.remove(id);
    }

    /**
     * 对使用了 @PulsarFlow 注解实现环绕切面
     *
     * @param joinPoint 切面连接点
     * @return 响应数据对象
     * @throws Throwable 可抛出异常
     */
    @Around("@annotation(com.github.yizzuide.milkomeda.pulsar.PulsarFlow)")
    Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 检测方法返回值
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String invokeMethodName = joinPoint.getSignature().getName();
        if (methodSignature.getReturnType() != Object.class) {
            throw new ClassCastException("You must set [Object] return type on method " +
                    invokeMethodName);
        }

        // 获取注解信息
        PulsarFlow pulsarFlow = getAnnotation(joinPoint, PulsarFlow.class);

        // 如果没有设置DeferredResult，则使用WebAsyncTask
        if (!pulsarFlow.useDeferredResult()) {
            // 返回异步任务
            WebAsyncTask<Object> webAsyncTask = new WebAsyncTask<>(new WebAsyncTaskCallable(joinPoint));
            if (null != timeoutCallback) {
                webAsyncTask.onTimeout(() -> this.timeoutCallback);
            }
            return webAsyncTask;
        }

        // 使用DeferredResult方式
        DeferredResult<Object> deferredResult = new DeferredResult<>();
        if (null != timeoutCallback) {
            // 适配超时处理
            deferredResult.onTimeout(() -> deferredResult.setResult(this.timeoutCallback.get()));
        }

        // 设置DeferredResult的错误处理（交给统一异常处理响应）
        /*if (null != errorCallback) {
            deferredResult.onError((throwable) -> deferredResult.setErrorResult(errorCallback.apply(throwable)));
        }*/

        // 创建增强DeferredResult
        PulsarDeferredResult pulsarDeferredResult = new PulsarDeferredResult();
        pulsarDeferredResult.setDeferredResult(deferredResult);

        // 准备设置DeferredResultID
        String id = pulsarFlow.id();
        String idValue = null;
        if (!StringUtils.isEmpty(id)) {
            // 解析表达式
            idValue = extractValue(joinPoint, id);
            pulsarDeferredResult.setDeferredResultID(idValue);
            // 注解设置成功，放入容器
            putDeferredResult(pulsarDeferredResult);
        }

        // 调用方法实现
        Object returnObj = joinPoint.proceed(injectParam(joinPoint, pulsarDeferredResult, pulsarFlow,
                StringUtils.isEmpty(idValue)));

        // 方法有返回值且不是DeferredResult，则不作DeferredResult处理
        if (null != returnObj && !(returnObj instanceof DeferredResult)) {
            // 通过注解存放过，则删除
            if (null != idValue) {
                removeDeferredResult(idValue);
            }
            return returnObj;
        }

        // 检查是否设置标识
        if (null == pulsarDeferredResult.getDeferredResultID()) {
            throw new IllegalArgumentException("You must invoke setDeferredResultID method of PulsarDeferredResult parameter on method " +
                    invokeMethodName);
        }

        // 如果注解没有设置，在方法设置后放入容器
        if (null == idValue) {
            putDeferredResult(pulsarDeferredResult);
        }

        // 无论超时还是成功响应，删除这个DeferredResult
        deferredResult.onCompletion(() -> removeDeferredResult(pulsarDeferredResult.getDeferredResultID()));

        // 返回
        return deferredResult;
    }

    /**
     * 基于WebAsyncTask实现的Callable
     */
    private static class WebAsyncTaskCallable implements Callable<Object> {
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
                    // 错误处理（交给统一异常处理响应）
                    /*if (null != errorCallback) {
                        return errorCallback.apply(t);
                    }*/
                    return ResponseEntity.status(500).body(t.getMessage());
                }
            }
        }
    }

    /**
     * 提交一个基于Spring的ThreadPoolTaskExecutor任务运行
     *
     * @param runnable Runnable
     */
    public void post(Runnable runnable) {
        applicationTaskExecutor.execute(runnable);
    }

    /**
     * 配置默认的Spring MVC异步支持
     *
     * @param configurer 配置对象
     * @param timeout    超时时间，ms
     * @deprecated since 1.16.0，因为SpringBoot 2.1.0版本开始默认已装配
     */
    public void configure(AsyncSupportConfigurer configurer, long timeout) {
        configure(configurer, 5, 10, 200, 100, timeout);
    }

    /**
     * 自定义配置的异步支持
     *
     * @param configurer       配置对象
     * @param corePoolSize     核心池大小
     * @param maxPoolSize      最大线程池数
     * @param queueCapacity    队列容量
     * @param keepAliveSeconds 线程保存存活时间
     * @param timeout          超时时间，ms
     * @deprecated since 1.16.0，因为SpringBoot 2.1.0版本开始默认已装配
     */
    public void configure(AsyncSupportConfigurer configurer, int corePoolSize, int maxPoolSize, int queueCapacity, int keepAliveSeconds, long timeout) {
        // 默认超时时间
        configurer.setDefaultTimeout(timeout);
        ThreadUtil.configTaskExecutor(applicationTaskExecutor, "pulsar-", corePoolSize, maxPoolSize, queueCapacity, keepAliveSeconds);
        configurer.setTaskExecutor(applicationTaskExecutor);
    }

    /**
     * 处理超时
     *
     * @param timeoutCallback 需要返回响应的超时数据
     */
    public void setTimeoutCallback(Supplier<Object> timeoutCallback) {
        this.timeoutCallback = timeoutCallback;
    }

}
