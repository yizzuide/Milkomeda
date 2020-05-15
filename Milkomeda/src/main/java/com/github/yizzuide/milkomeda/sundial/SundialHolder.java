package com.github.yizzuide.milkomeda.sundial;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据源处理
 * @author jsq 786063250@qq.com
 * @since 3.4.0
 * @since 3.5.0
 * Create at 2020/5/8
 */
@Slf4j
public class SundialHolder {

    /**
     * 使用ThreadLocal维护变量，ThreadLocal为每个使用该变量的线程提供独立的变量副本，
     *  所以每一个线程都可以独立地改变自己的副本，而不会影响其它线程所对应的副本。
     */
    private static final ThreadLocal<String> CONTEXT_HOLDER = new ThreadLocal<>();

    /**
     * 设置数据源key
     * @param dsType 数据源key
     */
    public static void setDataSourceType(String dsType) {
        CONTEXT_HOLDER.set(dsType);
        log.debug("Sundial selected datasource key: {}", dsType);
    }

    /**
     * 获得数据源key
     * @return 数据源key
     */
    public static String getDataSourceType()
    {
        return CONTEXT_HOLDER.get();
    }

    /**
     * 清空数据源key
     */
    public static void clearDataSourceType() {
        CONTEXT_HOLDER.remove();
    }
}
