package com.github.yizzuide.milkomeda.demo.halo.domain;

import lombok.Data;

import java.util.Date;
import java.io.Serializable;

/**
 * 订单表(TOrder)实体类
 *
 * @author makejava
 * @since 2020-01-30 19:32:31
 */
@Data
public class TOrder implements Serializable {
    private static final long serialVersionUID = -67250475608487726L;
    
    private Long id;
    /**
    * 用户id
    */
    private Long userId;
    /**
    * 订单号
    */
    private Long orderNo;
    /**
    * 产品id
    */
    private Long productId;
    /**
    * 产品名
    */
    private String productName;
    /**
    * 价格（分）
    */
    private Long price;
    
    private Date createTime;
    
    private Date updateTime;

}