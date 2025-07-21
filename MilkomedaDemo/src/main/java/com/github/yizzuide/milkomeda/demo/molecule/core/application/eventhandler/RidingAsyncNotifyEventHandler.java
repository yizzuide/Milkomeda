package com.github.yizzuide.milkomeda.demo.molecule.core.application.eventhandler;

import com.github.yizzuide.milkomeda.demo.molecule.core.domain.event.RidingOrderCreatedEvent;
import com.github.yizzuide.milkomeda.demo.molecule.core.uinterface.retry.RetryFailedException;
import com.github.yizzuide.milkomeda.orbit.orbit.OrbitAround;
import com.github.yizzuide.milkomeda.orbit.orbit.OrbitHandler;
import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 打车事件异步通知处理器
 *
 * @author yizzuide
 * Create at 2025/07/11 02:24
 */
@Slf4j
@OrbitHandler
@Component
public class RidingAsyncNotifyEventHandler {

    @OrbitAround(afterTransactionCommit = true)
    public void handle(RidingOrderCreatedEvent event) {
        log.info("订单异步通知，订单号：{}", event.getOrderNo());
        AopContextHolder.self(this.getClass()).notifyOrderPayToSource(event.getOrderNo());
    }

    @Async
    @Retryable(
            label = "同步支付通知给来源方",
            recover = "recoverPush",
            retryFor = RetryFailedException.class,
            listeners = "pushRetryListener",
            maxAttempts = 2,
            backoff = @Backoff(delay = 2000, multiplier = 1.0)
    )
    public void notifyOrderPayToSource(String orderNo) {
        log.info("同步支付通知给来源方，订单号：{}", orderNo);
        RetryContext context = RetrySynchronizationManager.getContext();
        if (context != null) {
            int retryCount = context.getRetryCount();
            log.info("重试[{}]次同步支付通知给来源方，订单号：{}", retryCount, orderNo);
        }
        throw new RetryFailedException("测试失败", 0, orderNo);
    }

    @Recover
    public void recoverPush(RetryFailedException e, String orderNo) {
        log.info("推送订单[orderNo={}]异常", orderNo);
    }
}
