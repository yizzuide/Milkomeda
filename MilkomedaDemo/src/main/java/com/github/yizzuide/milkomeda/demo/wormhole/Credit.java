package com.github.yizzuide.milkomeda.demo.wormhole;

import lombok.Data;

/**
 * Credit
 * 领域业务模块（聚合根）
 *
 * @author yizzuide
 * <br>
 * Create at 2020/05/05 15:51
 */
@Data
public class Credit {
    private String orderId;
    private Long userId;
    private Long quota;

    /**
     * 修改额度
     * @param quota 更新额度
     */
    public void updateQuota(Long quota) {
        this.setQuota(quota);
    }
}
