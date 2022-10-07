
package com.github.yizzuide.milkomeda.demo.halo.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * EventTracker
 *
 * @author yizzuide
 * <br />
 * Create at 2022/02/04 23:07
 */
@Slf4j
@Component
public class EventTracker {
    @EventListener
    public void onOrderCreatedEventSync(OrderCreatedEvent event) {
        // 使用回调监听事件提交完成，@TransactionalEventListener就是通过这个来实现
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                log.info("订单创成完成Sync：{}", event.getOrder());
            }
        });
    }


    // 异步事件监听
    @Async
    @TransactionalEventListener // 支持事务提交完成后执行
    public void onOrderCreatedEvent(OrderCreatedEvent event) {
        log.info("订单创成完成：{}", event.getOrder());
    }
}
