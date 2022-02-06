package com.github.yizzuide.milkomeda.demo.pillar.state;

import com.github.yizzuide.milkomeda.demo.light.pojo.Order;
import com.github.yizzuide.milkomeda.pillar.PillarState;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * OrderState
 * 针对业务简单的分流处理使用Enum状态机
 *
 * @author yizzuide
 * Create at 2022/02/06 16:11
 */
@Slf4j
public enum OrderState implements PillarState {

    ORDER_CREATED(0) {
        @Override
        public Map<String, Object> buildParams(Order order) {
            log.info("构建订单生成参数");
            Map<String, Object> params = new HashMap<>();
            params.put("api", "order_event_created");
            params.put("order_id", order.getOrderId());
            params.put("create_time", order.getCreateTime());
            params.put("buyer", "");
            return params;
        }
    },

    ORDER_PAY(1) {
        @Override
        public Map<String, Object> buildParams(Order order) {
            log.info("构建订单支付参数");
            Map<String, Object> params = new HashMap<>();
            params.put("api", "order_event_pay");
            params.put("order_id", order.getOrderId());
            params.put("amount", order.getAmount());
            params.put("buyer", "");
            return params;
        }
    };

    /**
     * 同步类型
     */
    private final int state;

    OrderState(int state) {
        this.state = state;
    }

    @Override
    public String getState() {
        return String.valueOf(state);
    }

    /**
     * 构建请求参数
     * @return  请求参数
     */
    public abstract Map<String, Object> buildParams(Order order);
}
