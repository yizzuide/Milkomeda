package com.github.yizzuide.milkomeda.comet.collector;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * CometCollectorHandler
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/29 16:53
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Component
public @interface CometCollectorHandler {
    /**
     * 同tag
     * @return  String
     */
    @AliasFor("tag")
    String value() default "";

    /**
     * 处理器的标签名
     * @return  String
     */
    @AliasFor("value")
    String tag() default "";
}
