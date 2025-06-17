package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.application.eventhandler;

import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.aggregate.OrderAggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.AggregateType;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventWithId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.AsyncEventHandler;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service.AggregateStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;

/**
 * 订单异步事件通知（调用三方API、发送MQ消息等）
 *
 * @author yizzuide
 * Create at 2025/06/14 19:39
 */
@AggregateType(OrderAggregate.TYPE)
@Component
@Slf4j
public class OrderIntegrationEventSender implements AsyncEventHandler {

    @Autowired
    private AggregateStore aggregateStore;

    @Override
    public void handleEvent(EventWithId<Event> eventWithId) {
        Event event = eventWithId.event();
        // 重建聚合到当前事件版本（只有在需要聚合里的数据时才调用）
        OrderAggregate orderAggregate = loadAggregate(event, aggregateStore, OrderAggregate.class);
        // 转为IntegrationEvent，发送到ACL层
        Timestamp createdDate = event.getCreatedDate();
        String eventType = event.getEventType();
        log.info("async event:{}, date:{}, aggregate type:{}, aggregate id:{}, rider id:{}, place date:{}", eventType, createdDate,
                event.getAggregateType(), event.getAggregateId(), orderAggregate.getRiderId(), orderAggregate.getPlacedDate());
    }
}
