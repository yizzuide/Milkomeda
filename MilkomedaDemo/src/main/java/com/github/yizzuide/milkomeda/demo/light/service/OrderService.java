package com.github.yizzuide.milkomeda.demo.light.service;

import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
import com.github.yizzuide.milkomeda.fusion.Fusion;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * OrderService
 *
 * @author yizzuide
 * Create at 2019/07/01 17:08
 */
@Service
public class OrderService {
    /**
     * 模拟根据订单id查询
     * @param orderId  订单id
     * @return
     */
    @Fusion // 这个用于测试Fusion模块，当前Light模块不需要这个注解
    public Order findById(String orderId) {
        return new Order(orderId, "小明", "1200", new Date());
    }
}
