package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @TableName rm_order
 */
@TableName(value ="rm_order")
@Data
public class RmOrder {

    @TableId(type = IdType.INPUT)
    private Long id;

    private Integer version;

    private String status;

    private Long riderId;

    private BigDecimal price;

    private Long driverId;

    private Timestamp placedDate;

    private Timestamp acceptedDate;

    private Timestamp cancelledDate;

    private Timestamp completedDate;
}