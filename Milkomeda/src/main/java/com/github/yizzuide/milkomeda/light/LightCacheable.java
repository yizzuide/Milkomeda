package com.github.yizzuide.milkomeda.light;

import java.lang.annotation.*;

/**
 * LightCacheable
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 2.3.0
 * Create at 2019/12/18 14:35
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
public @interface LightCacheable {
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

    /**
     * 缓存策略
     * @return LightDiscardStrategy
     */
    LightDiscardStrategy discardStrategy() default LightDiscardStrategy.DEFAULT;

    /**
     * 新的缓存总是拷贝内置的配置（如果想针对某类型定制配置，设置为false，然后配置LightCache Bean，Bean名与注解属性value()相同）
     * @return bool
     */
    boolean copyDefaultConfig() default true;

    /**
     * 过期时间
     * @return  long 单位：s，默认不过期（不设置则走全局配置文件里的配置，默认也是不过期）
     */
    long expire() default -1;

    /**
     * 只缓存到一级缓存
     * @return 默认为false
     */
    boolean onlyCacheL1() default false;

    /**
     * 只缓存到二级缓存
     * @return 默认为false
     */
    boolean onlyCacheL2() default false;
}
