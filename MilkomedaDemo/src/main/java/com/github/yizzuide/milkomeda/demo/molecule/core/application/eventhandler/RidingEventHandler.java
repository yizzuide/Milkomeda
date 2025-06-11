package com.github.yizzuide.milkomeda.demo.molecule.core.application.eventhandler;

import com.github.yizzuide.milkomeda.demo.molecule.core.domain.event.RidingOrderCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 打车事件处理器
 *
 * @author yizzuide
 * Create at 2025/06/09 17:24
 */
@Component
public class RidingEventHandler {

    @EventListener
    public void handle(RidingOrderCreatedEvent event) {
        System.out.println("订单创建成功，订单号：" + event.getOrderNo());
        // 保存聚合...
        // 保存读模型（视图表）...
    }

}
