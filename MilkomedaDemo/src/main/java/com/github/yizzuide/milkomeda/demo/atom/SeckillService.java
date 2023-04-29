package com.github.yizzuide.milkomeda.demo.atom;

import com.github.yizzuide.milkomeda.atom.AtomLock;
import com.github.yizzuide.milkomeda.atom.AtomLockWaitTimeoutType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * SeckillService
 *
 * @author yizzuide
 * <br>
 * Create at 2020/05/07 00:13
 */
@Slf4j
@Service
public class SeckillService {
    // 模拟库存
    private static int count = 10;

    // 分布式锁
    // waitTime：锁获取等待时间ms，默认-1直到占有锁
    // leaseTime：占用锁时间，默认60秒（防止Redis方案的服务突然挂掉时锁无法释放问题，ZK方案不需要管这个设置）
    @AtomLock(key = "'seckill_' + #productId", waitTime = 10, leaseTime = 30000,
            waitTimeoutType = AtomLockWaitTimeoutType.FALLBACK, fallback = "#target.seckillFail(args[0], args[1])")
    public boolean seckill(Long userId, Long productId) {
        if (count > 0) {
            // 模拟锁等待超时
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignore) {
            }
            count--;
            log.info("用户: {}, 抢购商品: {} 成功，库存在还剩下: {}", userId, productId, count);
            return true;
        }
        log.info("用户: {}, 抢购商品: {} 失败", userId, productId);
        return false;
    }

    public boolean seckillFail(Long userId, Long productId) {
        log.warn("用户:{} 抢购商品:{} 锁等待超时，进入失败处理！", userId, productId);
        return false;
    }
}
