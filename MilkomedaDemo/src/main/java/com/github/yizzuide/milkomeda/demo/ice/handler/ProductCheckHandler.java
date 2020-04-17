package com.github.yizzuide.milkomeda.demo.ice.handler;

import com.github.yizzuide.milkomeda.demo.ice.pojo.Product;
import com.github.yizzuide.milkomeda.ice.*;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;

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
    public Job<Product> handle(Job<Product> productJob) {
      log.info("接收到Job: {}", productJob);
      // 测试处理失败
//      int i = 1 / 0;
        // 重新入队到新topic
        Product product = productJob.getBody();
        product.setDesc("再次审核");
        // 注意：重新入队的job，需要重新构建新的topic
        return IceHolder.getIce().build(product.getId(), "topic_product_check_again", product, Duration.ofSeconds(2));
    }

    @IceListener(topic = "topic_product_check_again")
    public void handleAgain(Product product) {
        log.info("接收到Product: {}", product);
    }

    // 该方法被上面SpEL调用
    public String topicName() {
        return "topic_product_check";
    }

    @IceTtrOverloadListener(topic = "#target.topicName()")
    public void ttrHandle(Job<Map<String, Object>> job) {
        log.error("trr overload with job: {}", job);
    }
}
