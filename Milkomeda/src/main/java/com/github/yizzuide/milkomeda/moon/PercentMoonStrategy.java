package com.github.yizzuide.milkomeda.moon;

/**
 * PercentMoonStrategy
 * 百分比分配 <br>
 *
 * 全部阶段总百分比为100，阶段赋值如下：<br>
 * 33% --> 33：阶段值为索引0
 * 70% --> 70：阶段值为索引1
 *
 * @author yizzuide
 * @since 2.6.0
 * Create at 2020/03/13 21:42
 */
public class PercentMoonStrategy implements MoonStrategy {
    /**
     * 总占百分
     */
    public static final int PERCENT = 100;

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
        p = (p + 1) % PERCENT;
        leftHandPointer.setCurrent(p);
        return leftHandPointer;
    }
}
