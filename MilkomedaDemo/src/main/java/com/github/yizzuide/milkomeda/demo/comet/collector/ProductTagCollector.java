package com.github.yizzuide.milkomeda.demo.comet.collector;

import com.github.yizzuide.milkomeda.comet.collector.CometCollectorHandler;
import com.github.yizzuide.milkomeda.comet.collector.TagCollector;
import com.github.yizzuide.milkomeda.comet.core.CometData;
import lombok.extern.slf4j.Slf4j;

/**
 * ProductTagCollector
 *
 * @author yizzuide
 * <br>
 * Create at 2020/03/29 17:16
 */
@Slf4j
// @CometCollectorHandler可以指定日志收集器的tag，不指定的情况下为默认的bean名，如：ProductTagCollector -> productTagCollector
@CometCollectorHandler(tag = "product-tag")
public class ProductTagCollector implements TagCollector {
    @Override
    public void prepare(CometData params) {
      log.info("prepare - {}", params);
    }

    @Override
    public void onSuccess(CometData params) {
        log.info("onSuccess - {}", params);
    }

    @Override
    public void onFailure(CometData params) {
        log.info("onFailure - {}", params);
    }
}
