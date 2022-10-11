
package com.github.yizzuide.milkomeda.demo.halo.service;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.demo.halo.event.OrderCreatedEvent;
import com.github.yizzuide.milkomeda.demo.halo.mapper.TOrderMapper;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * OrderService
 *
 * @author yizzuide
 * <br>
 * Create at 2022/02/04 22:58
 */
@Service
public class TOrderService {
    @Resource
    private TOrderMapper tOrderMapper;

    public TOrder queryByIdAndTime(Long id, Date date) {
        return tOrderMapper.queryByIdAndTime(id, new Date());
    }

    public List<TOrder> queryAll(TOrder example) {
        return tOrderMapper.queryAll(example);
    }

    public void save() {
        TOrder tOrder = new TOrder();
        tOrder.setUserId(2L);
        tOrder.setOrderNo(1435436546556098L);
        tOrder.setProductId(2L);
        tOrder.setProductName("产品x");
        tOrder.setPrice(100000L);
        Date now = new Date();
        tOrder.setCreateTime(now);
        tOrder.setUpdateTime(now);
        tOrderMapper.insert(tOrder);

        // 发布订单创建事件
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        orderCreatedEvent.setOrder(tOrder);
        ApplicationContextHolder.get().publishEvent(orderCreatedEvent);
    }
}
