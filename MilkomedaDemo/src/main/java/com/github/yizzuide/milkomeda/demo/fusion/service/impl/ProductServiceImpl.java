package com.github.yizzuide.milkomeda.demo.fusion.service.impl;

import com.github.yizzuide.milkomeda.demo.fusion.service.ProductService;
import com.github.yizzuide.milkomeda.demo.ice.pojo.Product;
import com.github.yizzuide.milkomeda.fusion.Fusion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * ProductServiceImpl
 *
 * @author yizzuide
 * Create at 2020/01/02 14:39
 */
@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    // 根据条件是否调用业务方法
    @Fusion(allowed = "T(com.github.yizzuide.milkomeda.demo.fusion.pref.Platform).checkActive()")
    @Override
    public long push(Product product) {
        log.info("正在推送新产品：{}", product.getName());
        return 1;
    }

    @Override
    public Product pull() {
        return new Product("1001", "绿茶", 1000);
    }
}
