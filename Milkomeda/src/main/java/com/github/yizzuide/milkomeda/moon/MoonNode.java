package com.github.yizzuide.milkomeda.moon;

import lombok.Data;

/**
 * MoonNode
 *
 * @author yizzuide
 * @since 2.2.0
 * Create at 2019/12/31 18:10
 */
@Data
public class MoonNode<T> {
    private T data;
    private MoonNode<T> next;
}
