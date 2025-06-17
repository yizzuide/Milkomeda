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

package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventType;
import lombok.Getter;
import lombok.ToString;

/**
 * 订单接受事件
 *
 * @author yizzuide
 * Create at 2025/06/17 15:26
 */
@EventType("ORDER_ACCEPTED")
@ToString(callSuper = true)
@Getter
public final class OrderAcceptedEvent extends Event {

    private final Long driverId;

    @JsonCreator
    public OrderAcceptedEvent(Long aggregateId, int version, String aggregateType, Long driverId) {
        super(aggregateId, version, aggregateType);
        this.driverId = driverId;
    }
}
