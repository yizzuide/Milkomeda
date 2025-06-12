package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.application.service;

import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.aggregate.OrderAggregate;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.uinterface.command.PlaceOrderCommand;
import com.github.yizzuide.milkomeda.molecule.core.event.DomainEventsDefer;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service.ApplicationService;
import org.springframework.stereotype.Service;

/**
 * OrderAppService
 *
 * @author yizzuide
 * Create at 2025/06/12 17:44
 */
@Service
public class OrderAppService extends ApplicationService {
    @DomainEventsDefer
    public String place(PlaceOrderCommand command) {
        // Read other aggregates...
        //loadAggregate(aggregateClass, aggregateId);

        OrderAggregate orderAggregate = loadAggregate(OrderAggregate.class, command);
        orderAggregate.place(command);
        return "OK";
    }
}
