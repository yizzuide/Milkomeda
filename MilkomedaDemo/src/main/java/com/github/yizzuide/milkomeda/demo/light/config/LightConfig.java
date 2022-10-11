package com.github.yizzuide.milkomeda.demo.light.config;

import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
import com.github.yizzuide.milkomeda.light.ThreadLocalScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LightConfig
 *
 * @author yizzuide
 * <br>
 * Create at 2022/03/11 01:24
 */
@Configuration
public class LightConfig {

    // 每次请求创建一次实例
    @ThreadLocalScope
    @Bean
    public Order order() {
        Order order = new Order();
        order.setOrderId(String.valueOf(System.currentTimeMillis()));
        return order;
    }
}
