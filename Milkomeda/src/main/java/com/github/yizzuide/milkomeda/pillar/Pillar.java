package com.github.yizzuide.milkomeda.pillar;

/**
 * Pillar
 * 分流柱接口
 *
 * @param <P> 参数
 * @param <R> 结果
 *
 * @author yizzuide
 * @since  0.2.0
 * Create at 2019/04/11 14:08
 */
public interface Pillar<P, R> {
    /**
     * 支持处理的类型
     * @return 处理类型
     */
    String supportType();

    /**
     * 开始处理
     * @param params 参数
     * @param result 结果
     */
    void process(P params, R result);
}
