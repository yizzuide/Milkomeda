package com.github.yizzuide.milkomeda.demo.atom;

import com.github.yizzuide.milkomeda.atom.AtomLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SeckillService
 *
 * @author yizzuide
 * Create at 2020/05/07 00:13
 */
@Slf4j
@Service
public class SeckillService {
    // 模拟库存
    private static int count = 10;

    // 支持分布式key参数采集
    // waitTime最多等待时间ms，默认-1直到占有锁
    // leaseTime最多占用锁时间，默认60秒（防止Redis方案的服务突然挂掉时锁无法释放问题，ZK方案不需要管这个设置）
    @AtomLock(key = "'seckill_' + #productId", waitTime = 10000, leaseTime = 20000)
    public boolean seckill(Long userId, Long productId) {
        if (count > 0) {
            count--;
            log.info("用户: {}, 抢购商品: {} 成功，库存在还剩下: {}", userId, productId, count);
            return true;
        }
        log.info("用户: {}, 抢购商品: {} 失败", userId, productId);
        return false;
    }
}
