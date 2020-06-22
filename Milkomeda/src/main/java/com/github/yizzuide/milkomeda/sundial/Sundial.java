package com.github.yizzuide.milkomeda.sundial;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * 数据源注解
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * Create at 2020/5/8
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Sundial {
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

    /**
     * 拆分类型
     * @return  ShardingType
     */
    ShardingType shardingType() default ShardingType.NONE;

    /**
     * 数据源节点表达式，用于分库的场景（表达式全局变量：table为表名，p为参数，fn为函数调用，函数库参考{@link com.github.yizzuide.milkomeda.sundial.ShardingFunction}）
     * @return String
     */
    String nodeExp() default "";

    /**
     * 分表表达式，用于分表（表达式全局变量：table为表名，p为参数，fn为函数调用，函数库参考{@link com.github.yizzuide.milkomeda.sundial.ShardingFunction}）
     * @return  String
     */
    String partExp()  default "";
}
