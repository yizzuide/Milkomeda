package com.github.yizzuide.milkomeda.demo.light.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
import com.github.yizzuide.milkomeda.demo.light.service.OrderService;
import com.github.yizzuide.milkomeda.light.Cache;
import com.github.yizzuide.milkomeda.light.Spot;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

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
    private Cache<String, Order> lightCache;

    @Resource
    private OrderService orderService;

    @RequestMapping("detail")
    public Order detail(String id) {
        // 设置缓存key
        String key = "order:" + id;
        // Spot<String, Order> spot = lightCache.get(key, String.class, Order.class);
        // 使用TypeReference支持的泛型更加强大，只是在当前简单的例子看不出来，如：TypeReference<List<Page<User>>>
        Spot<String, Order> spot = lightCache.get(key, new TypeReference<String>() {}, new TypeReference<Order>() {});
        if (spot == null) {
            // 从数据库获取
            Order order = orderService.findById(id);
            spot = new Spot<>(id, order);
            lightCache.set(key, spot);
            return order;
        }
        return spot.getData();
    }
}
