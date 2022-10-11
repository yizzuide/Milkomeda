package com.github.yizzuide.milkomeda.demo.light.pojo;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class Order {
    String orderId;
    String name;
    String amount;
    Integer state;
    Date createTime;
}
