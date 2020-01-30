package com.github.yizzuide.milkomeda.demo.fusion.service.impl;

import com.github.yizzuide.milkomeda.demo.fusion.pref.Platform;
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
    // allowed：判断条件
    // fallback：条件判断结果为false，调用反馈方法
    @Fusion(allowed = Platform.EL_CHECK_ACTIVE, fallback = "#target.pushNotCheck(args)")
    @Override
    public long push(Product product) {
        log.info("正在推送新产品：{}", product.getName());
        return 1;
    }
    // allowed条件为false，调用该反馈方法返回
    public long pushNotCheck(Product product) {
        log.info("非检测环境下不推送新产品：{}", product.getName());
        return 1;
    }

    @Override
    public Product pull() {
        return new Product("1001", "绿茶", 1000);
    }
}
