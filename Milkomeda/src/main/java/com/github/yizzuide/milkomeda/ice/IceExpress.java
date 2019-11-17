package com.github.yizzuide.milkomeda.ice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;

/**
 * IceExpress
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/17 19:03
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
class IceExpress {
    /**
     * 延迟bucket分组
     */
    private String topic;
    /**
     * 目标对象
     */
    private Object target;
    /**
     * 监听方法
     */
    private Method method;
}
