package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.application.converter;

import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.aggregate.OrderAggregate;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.model.RmOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * OrderConverter
 *
 * @author yizzuide
 * Create at 2025/06/14 18:17
 */
@Mapper
public interface OrderConverter {

    @Mapping(source = "aggregateId", target = "id")
    RmOrder order2Projection(OrderAggregate order);
}
