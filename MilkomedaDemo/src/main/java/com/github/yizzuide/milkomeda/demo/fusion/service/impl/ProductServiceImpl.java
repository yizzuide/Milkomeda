package com.github.yizzuide.milkomeda.demo.fusion.service.impl;

import com.github.yizzuide.milkomeda.demo.fusion.pref.Platform;
import com.github.yizzuide.milkomeda.demo.fusion.service.ProductService;
import com.github.yizzuide.milkomeda.demo.ice.pojo.Product;
import com.github.yizzuide.milkomeda.fusion.Fusion;
import com.github.yizzuide.milkomeda.fusion.FusionAllowedType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * ProductServiceImpl
 *
 * @author yizzuide
 * Create at 2020/01/02 14:39
 */
@Slf4j
@Service("productService")
public class ProductServiceImpl implements ProductService {
    @Autowired
    private MessageService messageService;

    // 修改返回值，通过APIResponseDataHandler.productAction(...)
    @Fusion(tag = "product-push")
    // 根据条件是否调用业务方法 allowed：判断条件；fallback：条件判断结果为false时调用的反馈方法
    @Fusion(allowed = Platform.EL_CHECK_ACTIVE, fallback = "#target.pushNotCheck(#product, #delay)")
    // allowedType：逻辑条件类型，默认为AND
    @Fusion(allowedType = FusionAllowedType.OR, allowed = Platform.EL_IS_TEST)
    @Override
    public long push(Product product, boolean delay) {
        log.info("正在推送新产品：{}, 是否延迟：{}", product.getName(), delay);
        messageService.send(product.getName());
        return 1;
    }

    // allowed条件为false，调用该反馈方法返回
    public long pushNotCheck(Product product, boolean delay) {
        log.info("非检测环境下不推送新产品：{}, 是否延迟：{}", product.getName(), delay);
        return -1;
    }

    @Override
    public Product pull() {
        return new Product("1001", "绿茶", 1000);
    }
}
