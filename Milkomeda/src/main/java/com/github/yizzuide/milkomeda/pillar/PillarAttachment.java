package com.github.yizzuide.milkomeda.pillar;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PillarAttachment
 *
 * 分流柱附件
 *
 * @since 1.7.0
 * @author yizzuide
 * Create at 2019/06/26 11:15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PillarAttachment<T, E> {

    /**
     * 分流柱
     */
    T pillar;

    /**
     * 业务数据
     */
    E data;


    public PillarAttachment(T pillar) {
        this(pillar, null);
    }
}
