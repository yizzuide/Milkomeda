package com.github.yizzuide.milkomeda.demo.pillar.web.controller;

import com.github.yizzuide.milkomeda.pillar.PillarEntryDispatcher;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * MarketAPIController
 *
 * @author yizzuide
 * Create at 2020/07/02 18:11
 */
@RestController
@RequestMapping("pillar")
public class MarketAPIController {

    // http://localhost:8091/pillar/market/api?method=api.v1.check
    @RequestMapping("market/api")
    public String handle(@RequestParam Map<String, Object> params) {
        String method = params.get("method").toString();
        // 根据传过来的method的标识，使用派发器来调用执行
        return PillarEntryDispatcher.dispatch("market", method, params);
    }

    // http://localhost:8091/pillar/market/api/api.v1.push
    @RequestMapping("market/api/{method}")
    public String handleCallback(@RequestParam Map<String, Object> params, @PathVariable("method") String code) {
        return PillarEntryDispatcher.dispatch("market", code, params);
    }
}
