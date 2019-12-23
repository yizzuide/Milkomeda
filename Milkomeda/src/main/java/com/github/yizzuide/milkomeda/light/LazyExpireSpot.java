package com.github.yizzuide.milkomeda.light;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * LazyExpireSpot
 *
 * @author yizzuide
 * Create at 2019/12/23 15:53
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class LazyExpireSpot<V, E> extends SortSpot<V, E> {
    /**
     * 过期时间戳
     */
    private Long expireTime;
}
