package com.github.yizzuide.milkomeda.light;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Spot
 *
 * 缓存数据
 *
 * @since 1.8.0
 * @author yizzuide
 * Create at 2019/06/28 13:49
 */
@Data
@NoArgsConstructor
public class Spot<V, E> {

    /**
     * 视图数据或标识符
     */
    private V view;

    /**
     * 业务数据
     */
    private E data;


    public Spot(V view, E data) {
        this.view = view;
        this.data = data;
    }

    public Spot(E data) {
        this(null, data);
    }
}
