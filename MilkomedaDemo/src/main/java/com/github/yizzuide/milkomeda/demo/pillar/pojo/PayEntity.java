package com.github.yizzuide.milkomeda.demo.pillar.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PayEntity
 * 支付实体
 *
 * @author yizzuide
 * <br />
 * Create at 2019/06/26 11:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayEntity {
    Long userId;
    String amount;
    int type;
}
