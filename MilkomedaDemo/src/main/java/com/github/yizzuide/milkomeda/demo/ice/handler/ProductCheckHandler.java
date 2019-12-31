package com.github.yizzuide.milkomeda.demo.ice.handler;

import com.github.yizzuide.milkomeda.demo.ice.pojo.Product;
import com.github.yizzuide.milkomeda.ice.IceHandler;
import com.github.yizzuide.milkomeda.ice.IceListener;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * ProductCheckHandler
 * 使用注解直接接收的方式
 *
 * @author yizzuide
 * Create at 2019/11/17 17:34
 */
@Slf4j
// 注解为Topic接收组件
@IceHandler
public class ProductCheckHandler {

    // 监听topic消息（支持SpEL)
    @IceListener(topic = "#target.topicName()")
    public void handle(List<Product> products) {
      log.info("products: {}", products);
    }

    // 该方法被上面SpEL调用
    public String topicName() {
        return "topic_product_check";
    }
}
