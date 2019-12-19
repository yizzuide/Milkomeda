package com.github.yizzuide.milkomeda.light;

import java.lang.annotation.*;

/**
 * LightCachePut
 *
 * @author yizzuide
 * @since 2.0.0
 * Create at 2019/12/18 14:29
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface LightCachePut {
    /**
     * 缓存实例名（不同的缓存类型应该设置不能的名字）
     * @return  String
     */
    String value() default "lightCache";

    /**
     * 缓存key
     * @return  String
     */
    String key() default "";

    /**
     * 缓存key前辍，与属性方法 key() 合成完整的key
     * @return String
     */
    String keyPrefix() default "";

    /**
     * 自定义构建缓存key，与属性方法 key() 二选一
     * @return  String
     */
    String gKey() default "";
}
