package com.github.yizzuide.milkomeda.light;

import java.lang.annotation.*;

/**
 * LightCacheable
 *
 * @author yizzuide
 * @since 2.0.0
 * Create at 2019/12/18 14:35
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
public @interface LightCacheable {
    /**
     * 缓存实例（目前未实现多个缓存实例）
     * @return  String
     */
    String value() default "";

    /**
     * 缓存key
     * @return  String
     */
    String key();

    /**
     * 缓存key前辍
     * @return String
     */
    String keyPrefix();

    /**
     * 自定义构建缓存key
     * @return  String
     */
    String gKey();

}
