package com.github.yizzuide.milkomeda.demo.pillar.service;

import com.github.yizzuide.milkomeda.pillar.PillarEntryHandler;
import com.github.yizzuide.milkomeda.pillar.PillarEntryPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MarketService
 *
 * @author yizzuide
 * Create at 2020/07/02 18:13
 */
@Slf4j
@PillarEntryHandler(tag = "market")
@Component
public class MarketServiceImpl implements MarketService {

    @PillarEntryPoint(code = "api.v1.check")
    public String check(Map<String, Object> params) {
        log.info("check: {}", params);
        return "OK";
    }

    @PillarEntryPoint(code = "api.v1.push")
    public String push(Map<String, Object> params) {
        log.info("push: {}", params);
        return "OK";
    }

    @Async
    public void process() {
    }
}
