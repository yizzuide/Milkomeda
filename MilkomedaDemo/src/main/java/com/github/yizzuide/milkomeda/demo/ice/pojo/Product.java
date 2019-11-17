package com.github.yizzuide.milkomeda.demo.ice.pojo;

import lombok.Data;

/**
 * Product
 *
 * @author yizzuide
 * Create at 2019/11/16 19:45
 */
@Data
public class Product {
    private String id;
    private String name;
    private float price;
    private String[] pics;
    private String desc;
}
