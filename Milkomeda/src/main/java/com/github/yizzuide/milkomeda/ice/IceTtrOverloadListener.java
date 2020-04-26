package com.github.yizzuide.milkomeda.ice;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * IceTtrOverloadListener
 * TTR重试超载监听器
 *
 * <pre>
 *     通过配置 `ice.enable-job-timer-distributed` 配合Dead queue的使用有以下情况：
 *     1. TTR重试超载监听器未添加或执行失败 + 未开启Dead queue：通过计算延迟因子继续重试，并有错误级别的日志打印
 *     2. TTR重试超载监听器未添加或执行失败 + 开启Dead queue：延迟任务进入Dead queue，原数据保留
 *     3. TTR重试超载监听器执行成功 + 未开启Dead queue：延迟任务和原数据都将被移除
 *     4. TTR重试超载监听器执行成功 + 开启Dead queue：延迟任务进入Dead queue，原数据保留
 * </pre>
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/04 11:42
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface IceTtrOverloadListener {
    /**
     * 监听的Topic
     *
     * @return topic name
     */
    @AliasFor("topic")
    String value() default "";

    /**
     * 监听的Topic
     *
     * @return topic name
     */
    @AliasFor("value")
    String topic() default "";
}
