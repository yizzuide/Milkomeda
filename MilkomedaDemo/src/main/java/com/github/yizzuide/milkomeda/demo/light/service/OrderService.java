package com.github.yizzuide.milkomeda.demo.light.service;

import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
import com.github.yizzuide.milkomeda.light.LightCacheEvict;
import com.github.yizzuide.milkomeda.light.LightCacheable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * OrderService
 *
 * @author yizzuide
 * Create at 2019/07/01 17:08
 */
@Slf4j
@Service
public class OrderService {

    private static final String G_KEY = "order_list";

    /**
     * 模拟根据订单id查询
     * @param orderId  订单id
     */
//    @Fusion // 这个用于测试Fusion模块，当前Light模块不需要这个注解
    @LightCacheable(keyPrefix = "order:", key = "#orderId")
    public Order findById(String orderId) {
        return new Order(orderId, "小明", "1200", new Date());
    }

    @LightCacheEvict(keyPrefix = "order:", key = "#orderId")
    public void  deleteById(String orderId) {
      log.info("删除订单：{}", orderId);
    }

    @LightCacheable(gKey = OrderService.G_KEY)
    public List<Map<String, Object>> findList() {
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", "1111111");
        map.put("name", "小明");
        map.put("amount", "12000");
        map.put("createTime", new Date());
        list.add(map);
        return list;
    }
}
