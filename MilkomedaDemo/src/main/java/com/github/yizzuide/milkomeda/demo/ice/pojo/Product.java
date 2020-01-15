package com.github.yizzuide.milkomeda.demo.ice.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Product
 *
 * @author yizzuide
 * Create at 2019/11/16 19:45
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String id;
    private String name;
    private float price;
    private String[] pics;
    private String desc;

    public Product(String id, String name, float price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
}
