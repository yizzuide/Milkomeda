package com.github.yizzuide.milkomeda.demo.fusion.controller;

import com.github.yizzuide.milkomeda.demo.fusion.service.ProductService;
import com.github.yizzuide.milkomeda.demo.ice.pojo.Product;
import com.github.yizzuide.milkomeda.fusion.Fusion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * FusionController
 *
 * @author yizzuide
 * Create at 2020/01/02 14:38
 */
@RestController
@RequestMapping("fusion")
public class FusionController {

    @Autowired
    private ProductService productService;

    @RequestMapping("product/push")
    public ResponseEntity<Void> push() {
        Product product = new Product();
        product.setName("绿茶");
        productService.push(product);
        return ResponseEntity.status(200).build();
    }

    // 修改返回值
    @Fusion
    @RequestMapping("product/pull")
    public Object pull() {
        return productService.pull();
    }
}
