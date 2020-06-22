package com.github.yizzuide.milkomeda.sundial;

/**
 * ShardingType
 *
 * @author yizzuide
 * @since 3.8.0
 * Create at 2020/06/16 09:39
 */
public enum ShardingType {
    /**
     * 不分库分表
     */
    NONE,
    /**
     * 分库
     */
    SCHEMA,
    /**
     * 分表
     */
    TABLE,
    /**
     * 分库分表
     */
    SCHEMA_TABLE
}
