package com.github.yizzuide.milkomeda.sundial;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @date: 2020/5/7
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe: 数据源
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SundialDynamicDataSource {
    /**
     * 选择数据源Key
     * @return String
     */
    @AliasFor("key")
    String value() default DynamicRouteDataSource.MASTER_KEY;

    /**
     * 监选择数据源Key
     * @return  String
     */
    @AliasFor("value")
    String key() default DynamicRouteDataSource.MASTER_KEY;
}
