package com.github.yizzuide.milkomeda.demo.orbit;

import com.github.yizzuide.milkomeda.demo.sundial.mapper.TOrder2Mapper;
import com.github.yizzuide.milkomeda.orbit.orbit.OrbitProxy;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * OrderAPI
 *
 * @author yizzuide
 * <br>
 * Create at 2022/02/21 02:14
 */
@Slf4j
@Service
public class OrderAPI {

    @Resource
    private TOrder2Mapper tOrder2Mapper;

    public void fetchOrder(String orderNo) {
        log.info("正在请求第三方订单: {}", orderNo);
    }


    @OrbitProxy
    @Transactional
    public void pushOrder(String orderNo) {
        log.info("正在推送订单：{}", orderNo);
        tOrder2Mapper.findByOrderNo(Long.parseLong(orderNo));
    }
}
