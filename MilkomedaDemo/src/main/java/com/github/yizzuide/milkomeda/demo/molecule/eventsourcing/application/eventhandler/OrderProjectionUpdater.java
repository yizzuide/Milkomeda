package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.application.eventhandler;

import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.application.converter.OrderConverter;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.aggregate.OrderAggregate;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.event.OrderPlacedEvent;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.model.RmOrder;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.model.RmOrderRoute;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.service.RmOrderRouteService;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.service.RmOrderService;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.Aggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.AggregateType;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventAction;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventWithId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.SyncEventHandler;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单视图表同步更新处理器
 *
 * @author yizzuide
 * Create at 2025/06/12 21:04
 */
@AggregateType("ORDER")
@Component
public class OrderProjectionUpdater implements SyncEventHandler {

    @Autowired
    private RmOrderService rmorderService;

    @Autowired
    private RmOrderRouteService  rmorderRouteService;

    @Override
    public void handleEvents(List<EventWithId<Event>> events, Aggregate aggregate) {
        SyncEventHandler.super.handleEvents(events, aggregate);

        OrderAggregate orderAggregate = (OrderAggregate) aggregate;
        RmOrder rmOrder = Mappers.getMapper(OrderConverter.class).order2Projection(orderAggregate);
        rmorderService.save(rmOrder);
    }

    @EventAction
    public void handleEvent(OrderPlacedEvent orderPlacedEvent) {
        List<RmOrderRoute> routes = orderPlacedEvent.getRoute()
                .stream()
                .map(r -> {
            RmOrderRoute route = new RmOrderRoute();
            route.setOrderId(orderPlacedEvent.getAggregateId());
            route.setLongitude(r.longitude());
            route.setLatitude(r.latitude());
            route.setAddress(r.address());
            return route;
        }).toList();
        rmorderRouteService.saveBatch(routes);
    }
}
