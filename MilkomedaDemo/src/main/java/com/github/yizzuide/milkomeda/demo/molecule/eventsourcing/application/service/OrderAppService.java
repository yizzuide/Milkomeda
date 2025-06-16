package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.application.service;

import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.aggregate.OrderAggregate;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.uinterface.command.PlaceOrderCommand;
import com.github.yizzuide.milkomeda.molecule.core.event.DomainEventsDefer;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service.ApplicationService;
import com.github.yizzuide.milkomeda.sundial.Sundial;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OrderAppService
 *
 * @author yizzuide
 * Create at 2025/06/12 17:44
 */
@Sundial(key = "pg")
@Transactional
@Service
public class OrderAppService extends ApplicationService {
    @DomainEventsDefer
    public OrderAggregate place(PlaceOrderCommand command) {
        // 加载其它聚合
        //loadAggregate(aggregateClass, aggregateId);

        // 领域服务校验...

        // 创建订单
        OrderAggregate orderAggregate = loadAggregate(OrderAggregate.class, command);
        orderAggregate.place(command);
        return orderAggregate;
    }
}
