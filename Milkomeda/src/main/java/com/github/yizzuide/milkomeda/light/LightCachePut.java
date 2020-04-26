package com.github.yizzuide.milkomeda.light;

import java.lang.annotation.*;

/**
 * LightCachePut
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 2.0.3
 * Create at 2019/12/18 14:29
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface LightCachePut {
    /**
     * 缓存实例名（不同的缓存类型应该设置不能的名字），支持EL表达式
     * @return  String
     */
    String value();

    /**
     * 缓存key，支持EL表达式
     * @return  String
     */
    String key();

    /**
     * 缓存key前辍，与属性方法 key() 合成完整的key
     * @return String
     */
    String keyPrefix() default "";

    /**
     * 缓存条件，需要使用EL表达式
     * @return String
     */
    String condition() default "";
}
