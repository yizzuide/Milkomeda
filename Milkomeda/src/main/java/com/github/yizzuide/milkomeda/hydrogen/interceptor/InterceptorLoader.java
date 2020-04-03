package com.github.yizzuide.milkomeda.hydrogen.interceptor;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenLoader;
import lombok.NonNull;

import java.util.List;

/**
 * InterceptorLoader
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/03 10:59
 */
public interface InterceptorLoader extends HydrogenLoader {

    /**
     * 加载拦截器
     * @param clazz     拦截器实现类
     * @param include   拦截的URL
     * @param exclude   排除的URL
     * @param order     排序
     */
    void load(@NonNull Class<?> clazz, List<String> include, List<String> exclude, int order);

    /**
     * 卸载拦截器
     * @param clazz     拦截器实现类
     */
    void unLoad(@NonNull Class<?> clazz);
}
