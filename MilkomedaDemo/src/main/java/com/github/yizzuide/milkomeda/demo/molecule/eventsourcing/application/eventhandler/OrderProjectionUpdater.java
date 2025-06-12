package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.application.eventhandler;

import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.AggregateType;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.Aggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventWithId;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.eventhandler.SyncEventHandler;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * OrderProjectionUpdater
 *
 * @author yizzuide
 * Create at 2025/06/12 21:04
 */
@AggregateType("ORDER")
@Component
public class OrderProjectionUpdater implements SyncEventHandler {
    @Override
    public void handleEvents(List<EventWithId<Event>> events, Aggregate aggregate) {

    }
}
