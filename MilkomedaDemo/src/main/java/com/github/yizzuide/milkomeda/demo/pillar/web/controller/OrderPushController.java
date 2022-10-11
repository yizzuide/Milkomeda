package com.github.yizzuide.milkomeda.demo.pillar.web.controller;

import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
import com.github.yizzuide.milkomeda.demo.pillar.state.OrderState;
import com.github.yizzuide.milkomeda.pillar.PillarState;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

/**
 * OrderPushController
 *
 * @author yizzuide
 * <br>
 * Create at 2022/02/06 16:28
 */
@Slf4j
@RestController
@RequestMapping("pillar")
public class OrderPushController {
    @RequestMapping("order/approve")
    public ResponseEntity<Map<String, Object>> pushApprove(String id) {
        // 模拟从数据库查询...
        Order order = new Order(id, "", "1000", 0, new Date());
        val params = PillarState.of(String.valueOf(order.getState()), OrderState.class, OrderState.values()).buildParams(order);
        log.info("发送给审核系统的订单的参数：{}", params);
        return ResponseEntity.ok(params);
    }
}
