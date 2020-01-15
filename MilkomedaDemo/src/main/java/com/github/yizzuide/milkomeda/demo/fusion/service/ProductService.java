package com.github.yizzuide.milkomeda.demo.fusion.service;

import com.github.yizzuide.milkomeda.demo.ice.pojo.Product;

/**
 * ProductService
 *
 * @author yizzuide
 * Create at 2020/01/02 14:38
 */
public interface ProductService {
    /**
     * 推送给第三方平台
     */
    void push(Product product);

    /**
     * 从第三方拉取
     * @return Product
     */
    Product pull();
}
