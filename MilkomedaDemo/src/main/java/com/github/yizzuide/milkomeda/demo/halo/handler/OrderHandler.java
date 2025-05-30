package com.github.yizzuide.milkomeda.demo.halo.handler;

import com.github.yizzuide.milkomeda.halo.HaloHandler;
import com.github.yizzuide.milkomeda.halo.HaloListener;
import com.github.yizzuide.milkomeda.halo.HaloMeta;
import com.github.yizzuide.milkomeda.halo.HaloType;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.mapping.SqlCommandType;
import org.springframework.scheduling.annotation.Async;

/**
 * OrderHandler
 * order表监听处理器
 *
 * @author yizzuide
 * <br>
 * Create at 2020/01/30 23:02
 */
@Slf4j
@HaloHandler
public class OrderHandler {

    // 前置监听
    @Async // 异步执行
    @HaloListener(tableName = "t_order", type = HaloType.PRE)
    public void handlePre(Object params, SqlCommandType commandType, String sql) { // 散装参数方式
        // params可能是实体类型、简单数据类型或Map
        log.info("handlePre - 监听到【t_order】表操作：{}，参数：{}, sql: {}", commandType, params, sql);
    }

    // 默认监听所有的表
    @HaloListener(type = HaloType.PRE)
    public void handlePreAll(HaloMeta haloMeta) {
        log.info("handlePreAll - 监听到【*{}】表操作：{}，参数：{}", haloMeta.getTableName(), haloMeta.getSqlCommandType(),
                haloMeta.getParam());
    }

    // 后置监听
    // 默认type = HaloType.POST
    @HaloListener
    public void handlePostAll(HaloMeta haloMeta) {
        log.info("handlePostAll - 监听到【{}】表操作：{}，参数：{}, 结果:{}", haloMeta.getTableName(), haloMeta.getSqlCommandType(),
                haloMeta.getParam(), haloMeta.getResult());
    }
}
