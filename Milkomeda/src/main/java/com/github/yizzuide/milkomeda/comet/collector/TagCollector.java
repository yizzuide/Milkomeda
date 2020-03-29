package com.github.yizzuide.milkomeda.comet.collector;

/**
 * TagCollector
 * 标签收集器
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/29 17:12
 */
public abstract class TagCollector implements Collector {
    @Override
    public String supportType() {
        return null;
    }
}
