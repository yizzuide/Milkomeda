package com.github.yizzuide.milkomeda.demo.metal;

import lombok.Data;

import java.math.BigDecimal;

/**
 * FeeRateConfig
 *
 * @author yizzuide
 * <br />
 * Create at 2020/05/22 00:10
 */
@Data
public class FeeRateConfig {
    private BigDecimal service;
    private BigDecimal inst;
}
