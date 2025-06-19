package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.application.service;

import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.aggregate.OrderAggregate;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.uinterface.command.AcceptOrderCommand;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.uinterface.command.PlaceOrderCommand;
import com.github.yizzuide.milkomeda.molecule.core.event.DomainEventsDefer;
import com.github.yizzuide.milkomeda.molecule.eventsourcing.postgresql.service.ApplicationService;
import com.github.yizzuide.milkomeda.sundial.Sundial;
import org.springframework.stereotype.Service;

/**
 * 打车订单应用服务
 *
 * @author yizzuide
 * Create at 2025/06/12 17:44
 */
@Sundial(key = "pg")
@Service
public class OrderAppService extends ApplicationService {

    @DomainEventsDefer // 自动发送领域事件
    public OrderAggregate place(PlaceOrderCommand command) {
        // 加载其它聚合
        //loadAggregate(aggregateClass, aggregateId);

        // 领域服务(跨聚合可利用逻辑)校验...
        //orderAuditDomainService.audit(command);

        // 创建订单（在这里不要调用存储层的保存操作，读模型（视图表）都交给SyncEventHandler）
        OrderAggregate orderAggregate = loadAggregate(OrderAggregate.class, command);
        orderAggregate.place(command);
        return orderAggregate;
    }

    @DomainEventsDefer
    public void accept(AcceptOrderCommand command) {
        OrderAggregate orderAggregate = loadAggregate(OrderAggregate.class, command);
        orderAggregate.accept(command);
    }
}
