package com.github.yizzuide.milkomeda.universe.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * HandlerMetaData
 * 处理器元数据
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 2.5.0
 * Create at 2019/11/18 14:12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HandlerMetaData {
    /**
     * 处理标识名
     */
    private String name;
    /**
     * 其它属性方法值
     */
    private Map<String, Object> attributes;
    /**
     * 处理目标对象
     */
    private Object target;
    /**
     * 处理方法
     */
    private Method method;
}
