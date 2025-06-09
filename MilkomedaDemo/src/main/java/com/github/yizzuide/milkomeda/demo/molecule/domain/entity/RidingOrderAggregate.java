/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.demo.molecule.domain.entity;

import com.github.yizzuide.milkomeda.demo.molecule.domain.event.RidingOrderCreatedEvent;
import com.github.yizzuide.milkomeda.demo.molecule.domain.value.RidingOrderStatus;
import com.github.yizzuide.milkomeda.demo.molecule.uinterface.command.PlaceCommand;
import com.github.yizzuide.milkomeda.molecule.agg.AbstractAggregateRoot;
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
