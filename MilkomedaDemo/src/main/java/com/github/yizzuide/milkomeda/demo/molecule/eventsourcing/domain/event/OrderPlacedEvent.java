package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.value.Waypoint;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventType;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

/**
 * OrderPlacedEvent
 *
 * @author yizzuide
 * Create at 2025/06/11 16:02
 */
@EventType("ORDER_PLACED")
@ToString(callSuper = true)
@Getter
public final class OrderPlacedEvent extends Event {
    private final Long riderId;
    private final BigDecimal price;
    private final List<Waypoint> route;

    @JsonCreator
    @Builder
    public OrderPlacedEvent(Long aggregateId,
                            int version,
                            String aggregateType,
                            Long riderId,
                            BigDecimal price,
                            List<Waypoint> route) {
        super(aggregateId, version, aggregateType);
        this.riderId = riderId;
        this.price = price;
        this.route = List.copyOf(route);
    }
}
