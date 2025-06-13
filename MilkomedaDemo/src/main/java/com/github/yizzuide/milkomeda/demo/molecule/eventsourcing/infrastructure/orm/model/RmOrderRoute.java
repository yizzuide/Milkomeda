package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @TableName rm_order_route
 */
@TableName(value ="rm_order_route")
@Data
public class RmOrderRoute {

    @TableId(type = IdType.INPUT)
    private Long orderId;

    private String address;

    private Double latitude;

    private Double longitude;
}