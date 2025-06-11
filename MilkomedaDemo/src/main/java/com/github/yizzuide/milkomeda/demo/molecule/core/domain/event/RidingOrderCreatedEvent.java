package com.github.yizzuide.milkomeda.demo.molecule.core.domain.event;

import com.github.yizzuide.milkomeda.demo.molecule.core.domain.value.RidingOrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 打车订单创建事件
 *
 * @author yizzuide
 * Create at 2025/06/09 17:18
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RidingOrderCreatedEvent  {
    private String orderNo;
    private Long userId;
    private Long totalAmount;
    private RidingOrderStatus status;
}
