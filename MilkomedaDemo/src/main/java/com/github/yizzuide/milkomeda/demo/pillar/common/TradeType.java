package com.github.yizzuide.milkomeda.demo.pillar.common;

import com.github.yizzuide.milkomeda.pillar.PillarType;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * TradeType
 * 交易类型
 *
 * @author yizzuide
 * Create at 2019/04/11 16:46
 */
@Getter
@AllArgsConstructor
public enum TradeType implements PillarType {
    // 支付
    PAY(1, "PAY"),
    // 充值
    RECHARGE(2, "RECHARGE"),
    ;

    private Integer type;
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
