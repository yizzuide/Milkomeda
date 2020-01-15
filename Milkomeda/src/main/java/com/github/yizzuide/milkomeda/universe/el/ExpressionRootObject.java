package com.github.yizzuide.milkomeda.universe.el;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * ExpressionRootObject
 * 自定义EL根对象
 *
 * @author yizzuide
 * @since 1.5.0
 * Create at 2019/05/30 22:10
 */
@Getter
@AllArgsConstructor
public class ExpressionRootObject {
    /**
     * 方法所属目标对象，通过<code>#this.object</code>获取
     */
    private final Object object;
    /**
     * 方法参数列表，通过<code>#this.args[index]</code>获取
     */
    private final Object[] args;
}