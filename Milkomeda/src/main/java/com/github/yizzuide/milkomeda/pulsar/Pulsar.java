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

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.engine.el.ELContext;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import com.github.yizzuide.milkomeda.util.StringExtensionsKt;
import io.netty.util.Timeout;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.Null;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.Order;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Pulsar
 * <p>
 * 用于存放和管理DeferredResult的容器，配合应用注解 <code>@PulsarFlow</code> 切面，
 * 并提供参数注入DeferredResult的包装类 <code>PulsarDeferredResult</code> 到请求参数列表。
 * </p>
 *
 * @since 0.1.0
 * @version 3.20.0
 * @author yizzuide
 * <br>
 * Create at 2019/03/29 10:36
 */
@Slf4j
@Aspect
@Order(66)
public class Pulsar implements ApplicationListener<ApplicationStartedEvent> {
    /**
     * DeferredResult容器
     */
    private final Map<String, PulsarDeferredResult> deferredResultMap;

    // Spring Boot 3.0: AsyncTaskExecutor类型支持虚拟线程和传统线程池
    /**
     * 线程池执行器（从Spring Boot 2.1.0 开始默认已经装配）
     * @see org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration
     */
    private AsyncTaskExecutor applicationTaskExecutor;

    /**
     * 初始化容器容量大小
     */
    private static final int DEFAULT_CAPACITY = 64;

    Pulsar() {
        deferredResultMap = new ConcurrentHashMap<>(DEFAULT_CAPACITY);
        PulsarHolder.setPulsar(this);
    }

    @Override
    public void onApplicationEvent(@NotNull ApplicationStartedEvent event) {
        Map<String, AsyncTaskExecutor> taskExecutorMap = ApplicationContextHolder.get().getBeansOfType(AsyncTaskExecutor.class);
        if (taskExecutorMap.containsKey(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)) {
            applicationTaskExecutor = taskExecutorMap.get(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME);
        } else {
            applicationTaskExecutor = taskExecutorMap.values().stream().findFirst().orElse(null);
        }
        if (applicationTaskExecutor == null) {
            throw new BeanNotOfRequiredTypeException(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME, AsyncTaskExecutor.class, Null.class);
        }
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
     * 提交一个异步运行任务
     *
     * @param runnable 可运行任务
     * @since 1.1.0
     */
    public void post(Runnable runnable) {
        applicationTaskExecutor.execute(runnable);
    }

    /**
     * 提交一个异步任务，并返回结果
     *
     * @param callable  Callable
     * @param <T>   结果类型
     * @return  Future
     * @since 3.11.0
     */
    public <T> Future<T> postForResult(Callable<T> callable) {
        return applicationTaskExecutor.submit(callable);
    }

    /**
     * 提交一个延迟任务
     *
     * @param runnable      可运行任务
     * @param milliseconds  延迟 ms
     * @return  Timeout
     * @since 3.20.0
     */
    public Timeout delay(Runnable runnable, int milliseconds) {
        return PulsarHolder.hashedWheelTimer.newTimeout(timeout -> post(runnable), milliseconds, TimeUnit.MILLISECONDS);
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
        PulsarFlow pulsarFlow = ReflectUtil.getAnnotation(joinPoint, PulsarFlow.class);

        // 如果没有设置DeferredResult，则使用WebAsyncTask
        if (!pulsarFlow.useDeferredResult()) {
            return new WebAsyncTask<>(new WebAsyncTaskCallable(joinPoint));
        }

        // 使用DeferredResult方式
        DeferredResult<Object> deferredResult = new DeferredResult<>();

        // 创建增强DeferredResult
        PulsarDeferredResult pulsarDeferredResult = new PulsarDeferredResult();
        pulsarDeferredResult.setDeferredResult(deferredResult);

        // 准备设置DeferredResultID
        String id = pulsarFlow.id();
        String idValue = null;
        if (!StringExtensionsKt.isEmpty(id)) {
            // 解析表达式
            idValue = ELContext.getValue(joinPoint, id);
            pulsarDeferredResult.setDeferredResultID(idValue);
            // 注解设置成功，放入容器
            putDeferredResult(pulsarDeferredResult);
        }

        // 调用方法实现
        Object returnObj = joinPoint.proceed(ReflectUtil.injectParam(joinPoint, pulsarDeferredResult, pulsarFlow,
                StringExtensionsKt.isEmpty(idValue)));

        // 方法有返回值且不是DeferredResult，则不作DeferredResult处理
        if (null != returnObj && !(returnObj instanceof DeferredResult)) {
            // 通过注解存放过，则删除脏数据
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
        private final ProceedingJoinPoint joinPoint;

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
                    return ResponseEntity.status(500).body(t.getMessage());
                }
            }
        }
    }
}
