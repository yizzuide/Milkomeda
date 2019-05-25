package com.github.yizzuide.milkomeda.pulsar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PulsarFlow
 * 该注解能通过DeferredResult等技术延迟返回结果，让数据的返回更灵活，可应用于@RequestMapping注解的方法上
 *
 * 注意：使用该注解的方法返回必需为 Object。（由于 SpringMVC 内部会对类型进行校验的机制，使用 Object 并不会影响实际处理结果）
 *
 * @author yizzuide
 * @since  0.1.0
 * @version 1.4.0
 * Create at 2019/03/29 10:22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PulsarFlow {
    /**
     * 可选标注实际返回类型，只是让代码看起来更直观
     * @return 实际返回的Class
     */
     Class<?> returnClass() default Object.class;

    /**
     * 功能和returnClass一样
     * @return 实际返回的Class
     */
    Class<?> value() default Object.class;

    /**
     * 支持使用DeferredResult方式
     * 使用方法：
     * ``` @PulsarFlow(useDeferredResult=true)
     *     public Object method(..., PulsarDeferredResult deferredResult) {
     *          ...
     *          // 设置唯一标识符
     *          deferredResult.setDeferredResultID("235495954");
     *          // 返回任意数据，但内部不会获取这个值，在其它类中通过<code>PulsarHolder.getPulsar().getDeferredResult("唯一序列号").setResult(...)</code>返回
     *          return null;
     *     }
     * ```
     * 注意：WebAsyncTask方式会自动另起一个线程运行，而DeferredResult方式仍然运行在当前的Tomcat请求线程上！！
     *
     * @return 如果指定为 true，则使用DeferredResult，默认使用WebAsyncTask
     */
    boolean useDeferredResult() default false;
}
