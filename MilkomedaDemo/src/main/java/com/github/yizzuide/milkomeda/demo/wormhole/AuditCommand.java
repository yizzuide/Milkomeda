package com.github.yizzuide.milkomeda.demo.wormhole;

import lombok.Data;

/**
 * AuditCommand
 * 外部请求命令
 *
 *
 * @author yizzuide
 * <br>
 * Create at 2020/05/05 15:52
 */
@Data
public class AuditCommand {
    private String callbackId;
    private String orderId;
    private int state;
}
