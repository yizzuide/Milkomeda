package com.github.yizzuide.milkomeda.universe.el;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ExpressionRootObject
 * 表达式根对象
 *
 * @author yizzuide
 * @since 1.5.0
 * Create at 2019/05/30 22:10
 */
@Getter
@AllArgsConstructor
public class ExpressionRootObject {
    private final Object object;
    private final Object[] args;
}