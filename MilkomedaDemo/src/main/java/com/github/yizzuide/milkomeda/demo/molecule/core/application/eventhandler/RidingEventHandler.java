package com.github.yizzuide.milkomeda.demo.molecule.core.application.eventhandler;

import com.github.yizzuide.milkomeda.demo.molecule.core.domain.event.RidingOrderCreatedEvent;
import com.github.yizzuide.milkomeda.demo.sundial.mapper.TOrder2Mapper;
import com.github.yizzuide.milkomeda.orbit.orbit.OrbitProxy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 打车事件处理器
 *
 * @author yizzuide
 * Create at 2025/06/09 17:24
 */
@Slf4j
@Service
public class RidingEventHandler {

    @Resource
    private TOrder2Mapper tOrder2Mapper;

    @OrbitProxy
    @EventListener
    public void handle(RidingOrderCreatedEvent event) {
        log.info("订单创建成功，订单号：{}", event.getOrderNo());
        // 保存聚合相关读模型（视图表）...
        tOrder2Mapper.findByOrderNo(1435436546556099L);
    }

}
