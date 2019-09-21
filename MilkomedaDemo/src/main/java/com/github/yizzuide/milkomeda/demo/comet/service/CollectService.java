package com.github.yizzuide.milkomeda.demo.comet.service;

import com.github.yizzuide.milkomeda.comet.CometX;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * CollectService
 *
 * @author yizzuide
 * Create at 2019/09/21 13:26
 */
@Service
public class CollectService {

    // @CometX用于服务层的日志记录
    @CometX(name = "Collect保存")
    public boolean save(int state, Map<String, String> params) {
        // 用于测试异常
        int i = 1 / 0;
        return state == i;
    }
}
