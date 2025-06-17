package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.domain.value;

public enum OrderStatus {
    /**
     * 用户下单
     */
    PLACED,
    /**
     * 司机接单
     */
    ACCEPTED,
    /**
     * 订单完成
     */
    COMPLETED,
    /**
     * 订单取消
     */
    CANCELLED
}