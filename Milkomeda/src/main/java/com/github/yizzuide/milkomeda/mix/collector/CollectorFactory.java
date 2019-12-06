package com.github.yizzuide.milkomeda.mix.collector;

import com.github.yizzuide.milkomeda.comet.CometData;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import com.github.yizzuide.milkomeda.pillar.PillarRecognizer;
import com.github.yizzuide.milkomeda.pillar.PillarType;
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
     * 分流类型
     */
    private PillarType[] pillarTypes;

    /**
     * 根据标识符获取一个收集器
     * @param identifier    标识符
     * @return  收集器
     */
    public Collector get(Object identifier) {
        String pillarType = PillarRecognizer.typeOf(pillarTypes, identifier);
        return (Collector) pillarExecutor.getPillar(pillarType);
    }
}
