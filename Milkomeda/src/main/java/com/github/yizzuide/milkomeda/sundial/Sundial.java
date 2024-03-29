/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.sundial;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;


/**
 * 数据源注解
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * <br>
 * Create at 2020/5/8
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Sundial {
    /**
     * 选择数据源Key，使用时分以下两种情况：
     * <p>
     *     1. 仅使用主从策略时，需要配置<code>sundial.strategy</code>，再通过该属性设置RouteKey。<br>
     *     2. 如果是分库分表+主从策略时，需要配置<code>sundial.sharding.nodes</code>，再通过该属性设置RouteKey。
     * </p>
     * @return String
     */
    @AliasFor("key")
    String value() default DynamicRouteDataSource.MASTER_KEY;

    /**
     * 等同value的值
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
