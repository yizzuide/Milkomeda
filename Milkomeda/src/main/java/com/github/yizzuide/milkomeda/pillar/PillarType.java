package com.github.yizzuide.milkomeda.pillar;

/**
 * PillarType
 * 分流柱类型适配器通用接口，需要枚举实现（用于策略型扩展）
 *
 * @author yizzuide
 * @since 0.2.0
 * Create at 2019/04/11 18:19
 */
public interface PillarType {
    /**
     * 识别标识符，如：记录ID、序列号
     * @return Object
     */
    Object identifier();

    /**
     * pillar类型名
     * @return String
     */
    String pillarType();
}
