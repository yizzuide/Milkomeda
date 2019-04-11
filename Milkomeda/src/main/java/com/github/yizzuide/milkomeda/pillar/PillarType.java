package com.github.yizzuide.milkomeda.pillar;

/**
 * PillarType
 * 逻辑处理单元柱类型接口，需要枚举继承
 *
 * @author yizzuide
 * @since 0.2.0
 * Create at 2019/04/11 18:19
 */
public interface PillarType {
    /**
     * 识别标识符
     */
    Object identifier();

    /**
     * pillar类型名
     */
    String pillarType();
}
