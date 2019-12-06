package com.github.yizzuide.milkomeda.demo.light.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
import com.github.yizzuide.milkomeda.demo.light.service.OrderService;
import com.github.yizzuide.milkomeda.light.Cache;
import com.github.yizzuide.milkomeda.light.CacheHelper;
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
    private Cache lightCache;

    @Resource
    private OrderService orderService;

    @RequestMapping("detail")
    public Order detail(String orderId) {
        // 从缓存获取数据，支持一级缓存、二级缓存（如果使用默认配置的话）
//        return CacheHelper.get(lightCache, String.class, Order.class, orderId, (id) -> "order:" + id, (id) -> orderService.findById(id));
        // 使用TypeReference支持的泛型更加强大，只是在当前简单的例子看不出来，如：TypeReference<List<Page<User>>>
        return CacheHelper.get(lightCache, new TypeReference<Order>() {}, orderId, id -> "order:" + id, id -> orderService.findById(id));
    }

    @RequestMapping("detail2")
    public Order detail2() {
        // 从缓存获取数据，支持超级缓存、一级缓存、二级缓存（如果使用默认配置的话）
        return CacheHelper.get(lightCache, Order.class, (id) -> "order:" + id, (id) -> orderService.findById(id));
    }
}
