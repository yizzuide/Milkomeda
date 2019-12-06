package com.github.yizzuide.milkomeda.light;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * SortSpot
 *
 * 支持排序的热点数据
 *
 * @since 1.8.0
 * @version 1.17.0
 * @author yizzuide
 * Create at 2019/06/28 15:41
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class SortSpot<V, E> extends Spot<V, E> {
    /**
     * 缓存键
     */
    private String key;
}
