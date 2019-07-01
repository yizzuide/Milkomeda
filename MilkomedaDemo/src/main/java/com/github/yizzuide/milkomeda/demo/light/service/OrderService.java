package com.github.yizzuide.milkomeda.demo.light.service;

import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
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
     * @param orderId   订单id
     * @return
     */
    public Order findById(String orderId) {
        return new Order(orderId, "小明", "1200", new Date());
    }
}
