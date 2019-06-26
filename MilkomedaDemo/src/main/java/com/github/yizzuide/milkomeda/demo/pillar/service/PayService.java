package com.github.yizzuide.milkomeda.demo.pillar.service;

import com.github.yizzuide.milkomeda.demo.pillar.pojo.PayEntity;
import org.springframework.stereotype.Service;

/**
 * PayService
 *
 * @author yizzuide
 * Create at 2019/06/26 11:54
 */
@Service
public class PayService {

    public PayEntity get(String type) {
        return new PayEntity(12L, "1000", 1);
    }
}
