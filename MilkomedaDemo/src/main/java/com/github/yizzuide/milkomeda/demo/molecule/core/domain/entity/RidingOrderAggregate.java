package com.github.yizzuide.milkomeda.demo.molecule.core.domain.entity;

import com.github.yizzuide.milkomeda.demo.molecule.core.domain.event.RidingOrderCreatedEvent;
import com.github.yizzuide.milkomeda.demo.molecule.core.domain.value.RidingOrderStatus;
import com.github.yizzuide.milkomeda.demo.molecule.core.uinterface.command.PlaceCommand;
import com.github.yizzuide.milkomeda.molecule.core.agg.AbstractAggregateRoot;
import com.github.yizzuide.milkomeda.util.IdGenerator;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户打车订单聚合
 *
 * @author yizzuide
 * Create at 2025/06/09 17:08
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class RidingOrderAggregate extends AbstractAggregateRoot {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long totalAmount;
    private RidingOrderStatus status;

    public static RidingOrderAggregate create(PlaceCommand placeCommand) {
        RidingOrderAggregate ridingOrderAgg = new RidingOrderAggregate();
        ridingOrderAgg.setUserId(placeCommand.getUserId());
        ridingOrderAgg.setOrderNo(IdGenerator.genNext32ID());
        ridingOrderAgg.setTotalAmount(calculateTotalAmount(placeCommand.getFrom(), placeCommand.getTo()));
        ridingOrderAgg.setStatus(RidingOrderStatus.CREATED);
        // 在事件总线上注册领域事件
        RidingOrderCreatedEvent ridingOrderCreatedEvent = new RidingOrderCreatedEvent(
                ridingOrderAgg.getOrderNo(),
                ridingOrderAgg.getUserId(),
                ridingOrderAgg.getTotalAmount(),
                ridingOrderAgg.getStatus()
        );
        ridingOrderAgg.registerEvent(ridingOrderCreatedEvent);
        return ridingOrderAgg;
    }

    // 根据当前位置和目标位置计算价格
    private static Long calculateTotalAmount(String from, String to) {
        return 100L;
    }
}
