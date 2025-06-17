package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.aggregate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.event.OrderAcceptedEvent;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.event.OrderPlacedEvent;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.value.OrderStatus;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.value.Waypoint;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.uinterface.command.AcceptOrderCommand;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.uinterface.command.PlaceOrderCommand;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.Aggregate;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.agg.AggregateType;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.event.EventSourcing;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.exception.AggregateStateException;
import lombok.Getter;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.EnumSet;
import java.util.List;

/**
 * 订单聚合
 *
 * @author yizzuide
 * Create at 2025/06/11 16:00
 */
@AggregateType(OrderAggregate.TYPE)
@ToString(callSuper = true)
@Getter
public class OrderAggregate extends Aggregate {

    public static final String TYPE = "ORDER";

    private Long riderId;
    private Long driverId;
    private BigDecimal price;
    private List<Waypoint> route;
    private OrderStatus status;
    private Timestamp placedDate;
    private Timestamp acceptedDate;
    private Timestamp completedDate;
    private Timestamp cancelledDate;

    @JsonCreator
    public OrderAggregate(Long aggregateId, int version) {
        super(aggregateId, version);
    }

    public void place(PlaceOrderCommand command) {
        applyChange(new OrderPlacedEvent(aggregateId, getNextVersion(), this.getAggregateType(),  command.getRiderId(), command.getPrice(), command.getRoute()));
    }

    public void accept(AcceptOrderCommand command) {
        if (EnumSet.of(OrderStatus.ACCEPTED, OrderStatus.COMPLETED, OrderStatus.CANCELLED).contains(status)) {
            throw new AggregateStateException("Can't accept order in status %s", status);
        }
        applyChange(new OrderAcceptedEvent(aggregateId, getNextVersion(), this.getAggregateType(), command.getDriverId()));
    }

    @EventSourcing
    public void apply(OrderPlacedEvent event) {
        this.status = OrderStatus.PLACED;
        this.riderId = event.getRiderId();
        this.price = event.getPrice();
        this.route = event.getRoute();
        this.placedDate = event.getCreatedDate();
    }

    @EventSourcing
    public void apply(OrderAcceptedEvent event) {
        this.status = OrderStatus.ACCEPTED;
        this.driverId = event.getDriverId();
        this.acceptedDate = event.getCreatedDate();
    }
}
