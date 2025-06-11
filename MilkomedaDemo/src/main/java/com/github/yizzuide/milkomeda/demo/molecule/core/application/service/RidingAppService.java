package com.github.yizzuide.milkomeda.demo.molecule.core.application.service;

import com.github.yizzuide.milkomeda.demo.molecule.core.domain.entity.RidingOrderAggregate;
import com.github.yizzuide.milkomeda.demo.molecule.core.uinterface.command.PlaceCommand;
import com.github.yizzuide.milkomeda.molecule.core.event.DomainEventsDefer;
import org.springframework.stereotype.Service;

/**
 * 打车服务
 *
 * @author yizzuide
 * Create at 2025/06/09 17:04
 */
@Service
public class RidingAppService {

    @DomainEventsDefer // 自动发布领域事件
    public String place(PlaceCommand command) {
        // 从Repository加载聚合根...

        // 领域服务校验...

        // 创建订单
        RidingOrderAggregate aggregate = RidingOrderAggregate.create(command);
        return aggregate.getOrderNo();
    }
}
