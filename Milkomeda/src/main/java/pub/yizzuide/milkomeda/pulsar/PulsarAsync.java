package yiz.milkomeda.pulsar;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PulsarAsync
 * 开启异步，应用于@RequestMapping及子类别注解的 Restful 方法上，默认使用WebAsyncTask
 *
 * 注意：使用该注解的方法返回必需为 Object !!（由于 SpringMVC 内部会对类型进行校验的机制，使用 Object 并不会影响实际处理结果）
 *
 * @author yizzuide
 * Create at 2019/03/29 10:22
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PulsarAsync {
    /**
     * 用于WebAsyncTask方式实际类型标注，只是让代码看起来更容易理解
     * @return 实际返回的Class
     */
     Class<?> returnClass() default Object.class;

    /**
     * 功能和returnClass一样
     * @return 实际返回的Class
     */
    Class<?> value() default Object.class;

    /**
     * 支持使用DeferredResult异步处理
     * 注意：如果使用DeferredResult方式，需要在方法添加如下参数，如：
     *     ``` @PulsarAsync(useDeferredResult=true)
     *          public Object method(..., PulsarDeferredResult deferredResult) {
     *              ...
     *              // 设置唯一标识符
     *              deferredResult.setDeferredResultID("235495954");
     *              // 返回任意数据，但内部不会使用，请使用另一线程回调通过<code>pulsar.takeDeferredResult("235495954").setResult(obj)</code>返回
     *              return null;
     *          }
     *      ```
     * @return 如果指定为 true， 则使用DeferredResult，默认为 false，则使用WebAsyncTask
     */
    boolean useDeferredResult() default false;
}
