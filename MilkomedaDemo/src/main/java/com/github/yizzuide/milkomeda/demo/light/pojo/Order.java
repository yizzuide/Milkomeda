package com.github.yizzuide.milkomeda.demo.light.pojo;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Order
 * 订单实体
 *
 * @author yizzuide
 * <br>
 * Create at 2019/07/01 17:08
 */
@Data
@NoArgsConstructor
public class Order {
    private String orderId;
    private String name;
    private String amount;
    private Integer state;
    private Date createTime;

    public Order(String orderId, String name, String amount, Integer state, Date createTime) {
        this.orderId = orderId;
        this.name = name;
        this.amount = amount;
        this.state = state;
        this.createTime = createTime;
    }
}
