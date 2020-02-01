package com.github.yizzuide.milkomeda.demo.halo.handler;

import com.github.yizzuide.milkomeda.halo.HaloHandler;
import com.github.yizzuide.milkomeda.halo.HaloListener;
import com.github.yizzuide.milkomeda.halo.HaloType;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.SqlCommandType;

/**
 * OrderHandler
 * order表监听处理器
 *
 * @author yizzuide
 * Create at 2020/01/30 23:02
 */
@Slf4j
@HaloHandler
public class OrderHandler {

    // 前置监听
    @HaloListener(tableName = "t_order", type = HaloType.PRE)
    public void handlePre(Object param, SqlCommandType commandType) {
        // param可能是实体类型、简单数据类型、Map
        log.info("监听到【t_order】表操作：{}，参数：{}", commandType, param);
    }

    // 后置监听
    // 默认type = HaloType.POST
    @HaloListener(tableName = "t_order")
    public void handle(Object param, Object result, SqlCommandType commandType) {
        log.info("监听到【t_order】表操作：{}，参数：{}, 结果:{}", commandType, param, result);
    }
}
