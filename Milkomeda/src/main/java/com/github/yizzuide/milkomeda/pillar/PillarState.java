package com.github.yizzuide.milkomeda.pillar;

/**
 * PillarState
 * 状态机if/else拆分，需要枚举实现（用于确定的状态，不需要配合其它类）
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/02 17:29
 */
public interface PillarState {
    /**
     * 根据状态类型获取当前实例
     * @param state         状态机类型
     * @param type          枚举类
     * @param typeValues    枚举值
     * @param <T>           枚举类型
     * @return  枚举实例
     */
    static <T extends PillarState> T of(String state, Class<T> type, T[] typeValues) {
        for (T pillarState : typeValues) {
            if (pillarState.getState().equals(state)) {
                return pillarState;
            }
        }
        throw new IllegalArgumentException("Pillar Can't find state type: " + state);
    }

    /**
     * 获取当前状态值
     * @return  当前状态值
     */
    String getState();
}
