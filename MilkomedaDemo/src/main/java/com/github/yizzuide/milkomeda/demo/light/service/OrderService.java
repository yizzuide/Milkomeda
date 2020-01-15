package com.github.yizzuide.milkomeda.demo.light.service;

import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
import com.github.yizzuide.milkomeda.light.LightCacheEvict;
import com.github.yizzuide.milkomeda.light.LightCachePut;
import com.github.yizzuide.milkomeda.light.LightCacheable;
import com.github.yizzuide.milkomeda.light.LightDiscardStrategy;
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
    private static final long DEF_EXPIRE_SECONDS = 60;

    @LightCacheable(value = "orders", key = G_KEY)
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

    /**
     * 模拟根据订单id查询
     * @param orderId  订单id
     */
    // 参数采集方式生成缓存key
    @LightCacheable(value = "order", keyPrefix = "order:", key = "#orderId", condition = "#orderId!=null", discardStrategy = LightDiscardStrategy.LazyExpire)
    // 静态方法生成缓存key
//    @LightCacheable(value = "order", key = "T(com.github.yizzuide.milkomeda.demo.light.pref.CacheKeys).ORDER.key", condition = "#orderId!=null", discardStrategy = LightDiscardStrategy.LazyExpire, expire = DEF_EXPIRE_SECONDS)
    public Order findById(String orderId) {
        log.info("正在从数据库查询：{}", orderId);
        return new Order(orderId, "小明", "1200", new Date());
    }

    @LightCacheEvict(value = "order", keyPrefix = "order:", key = "#orderId")
    public void  deleteById(String orderId) {
      log.info("删除订单：{}", orderId);
    }

    @LightCachePut(value = "order", keyPrefix = "order:", key = "#orderId", condition = "#orderId!=null")
    public Order updateById(String orderId) {
        return new Order(orderId, "小红", "2000", new Date());
    }
}
