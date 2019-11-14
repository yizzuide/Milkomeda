package com.github.yizzuide.milkomeda.mix.collector;

import com.github.yizzuide.milkomeda.comet.CometData;

/**
 * CollectorAdapter
 * 收集器配置器
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/14 12:55
 */
public abstract class CollectorAdapter implements Collector {
    @Override
    public void process(CometData params, Object result) { }
}
