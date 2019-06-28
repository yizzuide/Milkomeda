package com.github.yizzuide.milkomeda.light;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * HotSpot
 *
 * 热点数据类型
 *
 * @since 1.8.0
 * @author yizzuide
 * Create at 2019/06/28 14:36
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class HotSpot<V, E> extends SortSpot<V, E> {

    /**
     * 热点数
     */
    private Long star;
}
