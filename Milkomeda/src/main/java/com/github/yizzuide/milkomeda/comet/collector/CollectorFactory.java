package com.github.yizzuide.milkomeda.comet.collector;

import com.github.yizzuide.milkomeda.comet.core.CometData;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import lombok.AllArgsConstructor;

/**
 * CollectorFactory
 * 收集器工厂
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 1.16.0
 * Create at 2019/11/13 19:11
 */
@AllArgsConstructor
public class CollectorFactory {
    /**
     * 分流执行器
     */
    private PillarExecutor<CometData, Object> pillarExecutor;

    /**
     * 根据标识符获取一个收集器
     * @param  tag  收集器的tag
     * @return  收集器
     */
    public Collector get(String tag) {
        return (Collector) pillarExecutor.getPillar(tag);
    }
}
