package com.github.yizzuide.milkomeda.demo.particle.service;

import com.github.yizzuide.milkomeda.particle.Limit;
import com.github.yizzuide.milkomeda.particle.Particle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * OrderNotifyService
 *
 * @author yizzuide
 * Create at 2025/10/31 11:24
 */
@Slf4j
@Service
public class OrderNotifyService {

    @Async
    @Limit(name = "notify2:order", key = "#orderId", limiterBeanName = "idempotentLimiter")
    public void notify(String orderId) throws InterruptedException {
        if (Particle.getContext().isLimited()) {
            log.warn("订单：{}已通知支付成功", orderId);
            return;
        }
        Thread.sleep(1000);
        log.info("订单：{}已支付成功", orderId);
    }
}
