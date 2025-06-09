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

package com.github.yizzuide.milkomeda.demo.molecule.application.service;

import com.github.yizzuide.milkomeda.demo.molecule.domain.entity.RidingOrderAggregate;
import com.github.yizzuide.milkomeda.demo.molecule.uinterface.command.PlaceCommand;
import com.github.yizzuide.milkomeda.molecule.event.DomainEventsNotifier;
import org.springframework.stereotype.Service;

/**
 * 网约车服务
 *
 * @author yizzuide
 * Create at 2025/06/09 17:04
 */
@Service
public class RidingAppService {

    @DomainEventsNotifier // 自动发布领域事件
    public String place(PlaceCommand command) {
        // 从事件朔源加载聚合根...

        // 领域服务校验...

        // 创建订单
        RidingOrderAggregate aggregate = RidingOrderAggregate.create(command.getUserId());
        return aggregate.getOrderNo();
    }
}
