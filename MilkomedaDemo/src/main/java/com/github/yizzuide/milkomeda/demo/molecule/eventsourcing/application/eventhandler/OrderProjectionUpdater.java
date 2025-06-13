package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.application.eventhandler;

import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.service.RmOrderRouteService;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.service.RmOrderService;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.AggregateType;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.Aggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventWithId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.SyncEventHandler;
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

    }
}
