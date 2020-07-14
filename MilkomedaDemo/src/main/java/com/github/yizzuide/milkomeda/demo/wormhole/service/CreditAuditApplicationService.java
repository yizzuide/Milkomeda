package com.github.yizzuide.milkomeda.demo.wormhole.service;

import com.github.yizzuide.milkomeda.demo.wormhole.Actions;
import com.github.yizzuide.milkomeda.demo.wormhole.AuditCommand;
import com.github.yizzuide.milkomeda.demo.wormhole.Credit;
import com.github.yizzuide.milkomeda.wormhole.WormholeEvent;
import com.github.yizzuide.milkomeda.wormhole.WormholeHolder;
import org.springframework.stereotype.Service;

/**
 * CreditAuditApplicationService
 * 信用审核应用服务
 *
 * @author yizzuide
 * Create at 2020/05/05 15:38
 */
@Service
public class CreditAuditApplicationService {

    // 审核完成回调（一个User Case在Application Service中对应一个处理方法）
    public void audit(AuditCommand auditCommand) {
        // 审核成功
        if (auditCommand.getState() == 0) {
            // 保存订单状态...

            // 模拟从Repository查询用户信用聚合根
            Credit credit = new Credit();
            credit.setOrderId(auditCommand.getOrderId());
            credit.setUserId(1001L);

            // 发送审核成功事件
            WormholeHolder.getEventBus().publish(new WormholeEvent<>("audit", credit), Actions.AUDIT_SUCCESS);
        }

    }
}
