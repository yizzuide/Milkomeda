/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.pillar;

import org.springframework.util.CollectionUtils;
import com.github.yizzuide.milkomeda.util.StringExtensionsKt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PillarExecutor
 * 逻辑分流中心执行器
 * 把很多的逻辑不同阶段的数据处理，拆分到不同的类里边去编写，这对于代码逻辑的解耦能起到非常好的作用，
 * 使用场景如：状态机转换、多if/else分支判断
 *
 * @param <P> 参数
 * @param <R> 结果
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 1.15.0
 * <br>
 * Create at 2019/04/11 14:14
 */
public class PillarExecutor<P, R> {
    /**
     * 分流柱集合
     */
    private final Set<Pillar<P, R>> pillars = new HashSet<>();

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
     * 根据类型名获取分流柱
     * <br><br>
     * PECS：Producer Extends Consumer Super
     * <br> - ? extends T: 只出不进（用于生产者）
     * <br> - ? super T: 只进不出（用于消费者）
     * @param type 分流类型
     * @return 分流柱集合
     */
    public List<? extends Pillar<P, R>> getPillars(String type) {
        if (StringExtensionsKt.isEmpty(type)) return null;
        return pillars.stream()
                .filter(processor -> type.equals(processor.supportType()))
                .collect(Collectors.toList());
    }

    /**
     * 根据类型名获取分流柱
     * @param type 分流类型
     * @return 分流柱集合
     */
    public Pillar<P, R> getPillar(String type) {
        List<? extends Pillar<P, R>> pillars = getPillars(type);
        if (CollectionUtils.isEmpty(pillars)) throw new IllegalArgumentException("type " + type +" not find");
        return pillars.get(0);
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
    public void addPillarList(List<? extends Pillar<P, R>> pillarList) {
        pillars.addAll(pillarList);
    }
}
