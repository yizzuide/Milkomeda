package com.github.yizzuide.milkomeda.demo.ice.controller;

import com.github.yizzuide.milkomeda.ice.Ice;
import com.github.yizzuide.milkomeda.demo.ice.pojo.Product;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ProductController
 *
 * @author yizzuide
 * Create at 2019/11/16 19:37
 */
@Slf4j
@RequestMapping("ice")
@RestController
public class ProductController {

    @Autowired
    private Ice ice;

    @RequestMapping("product/publish")
    public ResponseEntity publish(Product product) {
        log.info("正在上传商品：{}", product.getId());
        // 模拟审核商品。。
        if (product.getPics() == null) {
            log.info("当前商品没有上传图片，加入黑名单");
            ice.add(product.getId(), "topic_product_check",  product, 10000);
        }
        return ResponseEntity.ok("ok");
    }
}
