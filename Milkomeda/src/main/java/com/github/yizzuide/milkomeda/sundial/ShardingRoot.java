package com.github.yizzuide.milkomeda.sundial;

import lombok.Data;

/**
 * ShardingRoot
 * 表达式根对象
 *
 * @author yizzuide
 * @since 3.8.0
 * Create at 2020/06/16 14:20
 */
@Data
public class ShardingRoot {
    /**
     * 表名
     */
    private String table;
    /**
     * 参数
     */
    private Object p;
    /**
     * 函数
     */
    private ShardingFunction fn;
}
