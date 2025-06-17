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

package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.uinterface.command;

import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.aggregate.OrderAggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.AggregateType;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.command.BindAggregateId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.command.Command;
import lombok.Data;

/**
 * 司机接单命令
 *
 * @author yizzuide
 * Create at 2025/06/17 15:10
 */
@AggregateType(OrderAggregate.TYPE)
@Data
public class AcceptOrderCommand implements Command {
    private Long driverId;
    @BindAggregateId // 绑定聚合ID（修改类型必传，用于重建聚合）
    private Long orderId;
}
