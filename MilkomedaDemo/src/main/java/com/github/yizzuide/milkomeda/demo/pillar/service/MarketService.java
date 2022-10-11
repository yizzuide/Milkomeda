package com.github.yizzuide.milkomeda.demo.pillar.service;

import java.util.Map;

/**
 * MarketService
 *
 * @author yizzuide
 * <br>
 * Create at 2020/07/17 17:32
 */
public interface MarketService {
    String check(Map<String, Object> params);
    String push(Map<String, Object> params);
}
