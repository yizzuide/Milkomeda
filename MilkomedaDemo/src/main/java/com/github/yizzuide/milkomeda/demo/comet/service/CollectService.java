package com.github.yizzuide.milkomeda.demo.comet.service;

import com.github.yizzuide.milkomeda.comet.CometX;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.github.yizzuide.milkomeda.universe.context.AopContextHolder.getXCometData;
import static com.github.yizzuide.milkomeda.universe.context.AopContextHolder.self;

/**
 * CollectService
 *
 * @author yizzuide
 * Create at 2019/09/21 13:26
 */
@Slf4j
@Service
public class CollectService {

    // @CometX用于服务层的日志记录
    @CometX(name = "Collect保存")
    public boolean save(int state, Map<String, String> params) {
        System.out.println(getXCometData().getRequestTime());
        // 用于测试异常
//        int i = 1 / 0;
        // 代理对象调用，防切面失效
        self(this.getClass()).saveLog();
        return state == 1;
    }

    @CometX(name = "日志记录")
    public void saveLog() {
        log.info("save log...");
    }
}
