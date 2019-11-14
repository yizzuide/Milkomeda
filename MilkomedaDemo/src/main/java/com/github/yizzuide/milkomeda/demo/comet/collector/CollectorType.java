package com.github.yizzuide.milkomeda.demo.comet.collector;

import com.github.yizzuide.milkomeda.pillar.PillarType;
import lombok.AllArgsConstructor;

/**
 * CollectorType
 *
 * @author yizzuide
 * Create at 2019/11/14 15:59
 */
@AllArgsConstructor
public enum CollectorType implements PillarType {
    COLLECTOR_PROFILE("PROFILE", "COLLECT-PROFILE"),
    ;

    public static final String TAG_PROFILE = "PROFILE";

    private String type;
    private String typeName;

    @Override
    public Object identifier() {
        return type;
    }

    @Override
    public String pillarType() {
        return typeName;
    }
}
