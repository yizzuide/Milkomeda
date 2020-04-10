package com.github.yizzuide.milkomeda.comet.collector;

import com.github.yizzuide.milkomeda.comet.core.CometData;
import com.github.yizzuide.milkomeda.pillar.Pillar;

/**
 * Collector
 * 日志收集器接口
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 1.16.0
 * Create at 2019/11/13 18:12
 */
public interface Collector extends Pillar<CometData, Object> {

    @Override
    default void process(CometData params, Object result) {}

    /**
     * 数据准备
     * @param params CometData
     */
    void prepare(CometData params);

    /**
     * 执行成功
     * @param params CometData
     */
    void onSuccess(CometData params);

    /**
     * 执行失败
     * @param params CometData
     */
    void onFailure(CometData params);
}
