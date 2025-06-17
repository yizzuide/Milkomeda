package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.application.eventhandler;

import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.application.converter.OrderConverter;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.aggregate.OrderAggregate;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.event.OrderAcceptedEvent;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.event.OrderPlacedEvent;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.model.RmOrder;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.model.RmOrderRoute;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.service.RmOrderRouteService;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.service.RmOrderService;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.AggregateType;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventAction;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.SyncEventHandler;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 订单读模型（视图表）同步更新处理器
 *
 * @author yizzuide
 * Create at 2025/06/12 21:04
 */
@AggregateType(OrderAggregate.TYPE)
@Component
public class OrderProjectionUpdater implements SyncEventHandler {

    @Autowired
    private RmOrderService rmorderService;

    @Autowired
    private RmOrderRouteService  rmorderRouteService;

    @EventAction
    public void handleEvent(OrderPlacedEvent event) {
        OrderAggregate orderAggregate = loadAggregate(event.getAggregateId(), OrderAggregate.class);
        RmOrder rmOrder = Mappers.getMapper(OrderConverter.class).order2Projection(orderAggregate);
        rmorderService.save(rmOrder);

        List<RmOrderRoute> routes = event.getRoute()
                .stream()
                .map(r -> {
            RmOrderRoute route = new RmOrderRoute();
            route.setOrderId(event.getAggregateId());
            route.setLongitude(r.longitude());
            route.setLatitude(r.latitude());
            route.setAddress(r.address());
            return route;
        }).toList();
        rmorderRouteService.saveBatch(routes);
    }

    @EventAction
    public void handleEvent(OrderAcceptedEvent event) {
        OrderAggregate orderAggregate = loadAggregate(event.getAggregateId(), OrderAggregate.class);
        RmOrder rmOrder = rmorderService.getById(event.getAggregateId());
        rmOrder.setDriverId(orderAggregate.getDriverId());
        rmOrder.setStatus(orderAggregate.getStatus().name());
        rmOrder.setAcceptedDate(orderAggregate.getAcceptedDate());
        rmorderService.updateById(rmOrder);
    }
}
