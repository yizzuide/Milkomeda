package com.github.yizzuide.milkomeda.demo.ice.task;

import com.github.yizzuide.milkomeda.demo.ice.pojo.Product;
import com.github.yizzuide.milkomeda.ice.Ice;
import com.github.yizzuide.milkomeda.ice.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * ProductCheckTask
 * 自定义任务接收处理（API调用方式）
 *
 * @author yizzuide
 * <br>
 * Create at 2019/11/16 21:42
 */
@Slf4j
//@Component
public class ProductCheckTask {

//    @Autowired
    private Ice ice;

    // 使用多线程调度同一任务，使用前要开启@EnableAsync，如果线程池不叫taskExecutor，需要指定
//    @Async("pulsarTaskExecutor")
    // 使用前开启@EnableScheduling
//    @Scheduled(fixedRate = 1000)
    public void check() {
        processBatch();
    }

    // 批量处理
    private void processBatch() {
        try {
            // 从待处理队列获取（如果有10个，则取10个，不足取实际容量）
            List<Job<Product>> jobs = ice.pop("topic_product_check", 10);
            if (CollectionUtils.isEmpty(jobs)) {
                return;
            }
            for (Job<Product> job : jobs) {
                // 模拟处理中
                log.info("获得需要处理的黑名单商品：{}", job.getBody());
                Thread.sleep(1000);
            }
            // 完成处理
            ice.finish(jobs);
        } catch (Exception e) {
            log.error("DelayChecker error", e);
        }
    }
}
