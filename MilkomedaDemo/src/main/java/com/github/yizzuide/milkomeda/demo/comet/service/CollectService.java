package com.github.yizzuide.milkomeda.demo.comet.service;

import com.github.yizzuide.milkomeda.comet.core.CometX;
import com.github.yizzuide.milkomeda.comet.core.XCometContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * CollectService
 *
 * @author yizzuide
 * <br>
 * Create at 2019/09/21 13:26
 */
@Slf4j
@Service
public class CollectService {

    // @CometX用于服务层的日志记录
    @CometX(name = "收藏保存", tag = "collect", path = "collect:save", subjectId = "#params[uid]",
            success = "'状态'+{#state}+'保存为'+{#ret}", detail = "'时间：'+{#now()}+'操作人: '+{#ops}")
    public boolean save(int state, Map<String, String> params) {
        // 用于测试异常
//        int i = 1 / 0;
        XCometContext.putVariable("ops", 1);
        return state == 1;
    }
}
