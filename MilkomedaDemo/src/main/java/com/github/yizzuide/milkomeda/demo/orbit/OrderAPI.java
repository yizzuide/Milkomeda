package com.github.yizzuide.milkomeda.demo.orbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * OrderAPI
 *
 * @author yizzuide
 * Create at 2022/02/21 02:14
 */
@Slf4j
@Component
public class OrderAPI {

    public void fetchOrder(String orderNo) {
        log.info("正在请求第三方订单: {}", orderNo);
    }
}
