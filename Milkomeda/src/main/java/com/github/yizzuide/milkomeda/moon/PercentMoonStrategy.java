package com.github.yizzuide.milkomeda.moon;

import lombok.Data;

/**
 * PercentMoonStrategy
 * 百分比分配 <br>
 *
 * 全部阶段总百分比为100，阶段赋值如下：<br>
 * 33% 设置 33：阶段值为索引0
 * 67% 设置 67：阶段值为索引1
 *
 * @author yizzuide
 * @since 2.6.0
 * @version 2.7.3
 * Create at 2020/03/13 21:42
 */
@Data
public class PercentMoonStrategy implements MoonStrategy {
    /**
     * 默认总占百分
     */
    public static final int DEFAULT_PERCENT = 100;
    /**
     * 百分比
     */
    public int percent = DEFAULT_PERCENT;

    @Override
    public <T> T getCurrentPhase(Moon<T> moon) {
        throw new UnsupportedOperationException("PercentMoonStrategy is not support run on standalone.");
    }

    @SuppressWarnings("all")
    @Override
    public <T> T getPhase(String key, Integer p, Moon<T> prototype) {
        for (Integer i = 0; i < prototype.getLen(); i++) {
            T phaseName = prototype.getPhaseNames().get(i);
            if (!(phaseName instanceof Integer)) {
                throw new UnsupportedOperationException("Only support int data type.");
            }
            Integer phase = (Integer) phaseName;
            // 累计百分比，使各阶段分配相应百分比的值
            if (i != 0) {
                phase += (Integer) prototype.getPhaseNames().get(i - 1);
            }
            // 找到不满足百分比的阶段，返回阶段索引值
            if (p < phase) {
                return (T) Integer.valueOf(i);
            }
        }
        return null;
    }

    @Override
    public LeftHandPointer pluck(Moon<?> moon, LeftHandPointer leftHandPointer) {
        int p = leftHandPointer.getCurrent();
        p = (p + 1) % this.getPercent();
        leftHandPointer.setCurrent(p);
        return leftHandPointer;
    }

    /**
     * 百分比表达式解析, 固定百分总值为100。如：5/5、3/7、25/75
     * @param percentExpress    百分比表达式
     * @return 百分比列表
     */
    public static Integer[] parse(String percentExpress) {
        return parse(percentExpress, null);
    }

    /**
     * 百分比表达式解析, 并自动缩放百分总值 <br>
     * <pre>
     * 5/5、3/7：百分总值为10
     * 25/75：百分总值为100
     * </pre>
     * @param percentExpress    百分比表达式
     * @param strategy          PercentMoonStrategy
     * @return 百分比列表
     */
    public static Integer[] parse(String percentExpress, MoonStrategy strategy) {
        String[] percentComps = percentExpress.split("/");
        if (percentComps.length < 2) {
            throw new IllegalArgumentException("Percent express format is illegal: " + percentExpress);
        }
        Integer[] percentArray = new Integer[percentComps.length];
        for (int i = 0; i < percentComps.length; i++) {
            String percentComp = percentComps[i];
            if (percentComp.length() > 2 || percentComp.startsWith("0")) {
                throw new IllegalArgumentException("Percent express format is illegal: " + percentExpress);
            }
            if (percentComp.length() == 1) {
                if (strategy == null) {
                    // 补充到百分的比例
                    percentComps[i] = percentComp + "0";
                } else if (strategy instanceof PercentMoonStrategy){
                    // 缩小百分分配总值
                    ((PercentMoonStrategy) strategy).setPercent(10);
                }
            }
            percentArray[i] = Integer.valueOf(percentComps[i]);
        }
        return percentArray;
    }
}
