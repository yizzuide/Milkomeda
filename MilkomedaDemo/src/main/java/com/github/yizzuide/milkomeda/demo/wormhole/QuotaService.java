package com.github.yizzuide.milkomeda.demo.wormhole;

import com.github.yizzuide.milkomeda.wormhole.WormholeAction;
import com.github.yizzuide.milkomeda.wormhole.WormholeEvent;
import com.github.yizzuide.milkomeda.wormhole.WormholeEventHandler;
import com.github.yizzuide.milkomeda.wormhole.WormholeTransactionHangType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * QuotaService
 * 领域远程服务
 *
 * @author yizzuide
 * Create at 2020/05/05 15:39
 */
@Slf4j
@Service
@WormholeEventHandler
public class QuotaService {

    @Async
    @WormholeAction(value = Actions.AUDIT_SUCCESS, transactionHang = WormholeTransactionHangType.AFTER_COMMIT)
    public void onEvent(WormholeEvent<Credit> event) {
        // 更新额度
        event.getData().updateQuota(20000L);
        log.info("审核后修改用户额度: {}", event);
    }
}
