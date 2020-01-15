package com.github.yizzuide.milkomeda.demo.light.controller;

import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
import com.github.yizzuide.milkomeda.demo.light.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * OrderController
 *
 * @author yizzuide
 * Create at 2019/07/01 17:09
 */
@RestController
@RequestMapping("order")
public class OrderController {

    @Resource
    private OrderService orderService;

    @RequestMapping("detail")
    public Order detail(String orderId) {
        return orderService.findById(orderId);
    }

    @RequestMapping("del")
    public ResponseEntity<?> del(String orderId) {
        orderService.deleteById(orderId);
        return ResponseEntity.status(200).build();
    }

    @RequestMapping("update")
    public ResponseEntity<Order> update(String orderId) {
        return ResponseEntity.ok(orderService.updateById(orderId));
    }

    @RequestMapping("list")
    public List<Map<String, Object>> list() {
        return orderService.findList();
    }
}
