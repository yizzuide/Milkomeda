package com.github.yizzuide.milkomeda.pillar;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PillarExecutor
 * 逻辑分流中心执行器
 * 把很多的逻辑不同阶段的数据处理，拆分到不同的类里边去编写，这对于代码逻辑的解耦能起到非常好的作用，
 * 如：状态机转换、多if/else分支判断
 *
 * @param <P> 参数
 * @param <R> 结果
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 0.2.10
 * Create at 2019/04/11 14:14
 */
public class PillarExecutor<P, R> {
    /**
     * 分流柱集合
     */
    private Set<Pillar<P, R>> pillars = new HashSet<>();

    /**
     * 执行分流柱
     * @param type 分流类型
     * @param params 参数
     * @param result 结果
     */
    public void execute(String type, P params, R result) {
        for(Pillar<P, R> pillar : getPillars(type)) {
            pillar.process(params, result);
        }
    }

    /**
     * 根据类型获取分流柱
     * @param type 分流类型
     * @return 分流柱集合
     */
    public List<? extends Pillar<P, R>> getPillars(String type) {
        return pillars.stream()
                .filter(processor -> type.equals(processor.supportType()))
                .collect(Collectors.toList());
    }


    /**
     * 添加一个分流柱
     * @param pillar 逻辑处理
     */
    public void addPillar(Pillar<P, R> pillar) {
        pillars.add(pillar);
    }

    /**
     * 添加多个分流柱
     * @param pillarList 分流柱集合
     */
    public void addPillarList(List<Pillar<P, R>> pillarList) {
        pillars.addAll(pillarList);
    }
}
