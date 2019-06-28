package com.github.yizzuide.milkomeda.light;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

/**
 * TimelineSpot
 *
 * 时间线数据类型
 *
 * @since 1.8.0
 * @author yizzuide
 * Create at 2019/06/28 14:44
 */
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class TimelineSpot<V, E> extends SortSpot<V, E> {

    /**
     * 访问时间
     */
    private Date time;
}
