package com.github.yizzuide.milkomeda.demo.wormhole;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * AuditController
 * 领域适配器
 *
 * @author yizzuide
 * Create at 2020/05/05 15:36
 */
@RestController
@RequestMapping("audit")
public class AuditController {

    @Resource
    private CreditAuditApplicationService creditAuditApplicationService;

    @RequestMapping("callback")
    public Object audit(AuditCommand auditCommand) {
        creditAuditApplicationService.audit(auditCommand);
        return "OK";
    }
}
