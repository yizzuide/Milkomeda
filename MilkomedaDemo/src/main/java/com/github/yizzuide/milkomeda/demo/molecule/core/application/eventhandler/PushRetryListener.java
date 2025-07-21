package com.github.yizzuide.milkomeda.demo.molecule.core.application.eventhandler;

import com.github.yizzuide.milkomeda.demo.molecule.core.uinterface.retry.RetryFailedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

/**
 * 重试监听器
 *
 * @author yizzuide
 * Create at 2025/07/11 10:57
 */
@Slf4j
@Component
public class PushRetryListener implements RetryListener {
    @Override
    public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback, Throwable throwable) {
        int retryCount = context.getRetryCount();
        if (throwable instanceof RetryFailedException exception) {
            exception.setRetryCount(retryCount);
            log.info("重试第[{}]次，订单号:{}", retryCount, exception.getRecord());
        }
    }
}
