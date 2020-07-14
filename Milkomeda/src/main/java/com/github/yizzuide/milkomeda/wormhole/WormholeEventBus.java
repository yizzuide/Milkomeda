package com.github.yizzuide.milkomeda.wormhole;

import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

/**
 * WormholeEventBus
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.11.1
 * Create at 2020/05/05 14:30
 */
@Slf4j
@Data
public class WormholeEventBus {
    /**
     * 领域事件跟踪器
     */
    private List<WormholeEventTrack> trackers;

    /**
     * 发布领域事件
     * @param event     领域事件
     * @param action    动作名
     * @param <T>       领域事件数据类型
     */
    public <T> void publish(WormholeEvent<T> event, String action) {
        publish(event, action, null);
    }

    /**
     * 发布领域事件
     * @param event     领域事件
     * @param action    动作名
     * @param callback  回调
     * @param <T>       领域事件数据类型
     */
    public <T> void publish(WormholeEvent<T> event, String action,
                            WormholeCallback callback) {
        if (action == null || event == null) {
            return;
        }
        event.setAction(action);
        List<HandlerMetaData> handlerList = WormholeRegistration.getActionMap().get(action);
        if (!CollectionUtils.isEmpty(handlerList)) {
            for (HandlerMetaData handler : handlerList) {
                if (!action.equals(handler.getName())) {
                    continue;
                }
                boolean isAsync = (boolean) handler.getAttributes().get(WormholeRegistration.ATTR_ASYNC);
                WormholeTransactionHangType hangType = (WormholeTransactionHangType) handler.getAttributes().get(WormholeRegistration.ATTR_HANG_TYPE);
                // 非事件回调执行
                if (hangType == WormholeTransactionHangType.NONE) {
                    execute(isAsync, handler, event, action, callback);
                } else {
                    // 注删事务回调
                    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                        @Override
                        public void beforeCommit(boolean readOnly) {
                            if (hangType == WormholeTransactionHangType.BEFORE_COMMIT) {
                                execute(isAsync, handler, event, action, callback);
                            }
                        }

                        @Override
                        public void afterCommit() {
                            if (hangType == WormholeTransactionHangType.AFTER_COMMIT) {
                                execute(isAsync, handler, event, action, callback);
                            }
                        }

                        @Override
                        public void afterCompletion(int status) {
                            if (status == STATUS_ROLLED_BACK && hangType == WormholeTransactionHangType.AFTER_ROLLBACK) {
                                execute(isAsync, handler, event, action, callback);
                                return;
                            }
                            if (hangType == WormholeTransactionHangType.AFTER_COMPLETION) {
                                execute(isAsync, handler, event, action, callback);
                            }
                        }
                    });
                }
            }
        }

        // write into event store
        if (trackers != null) {
            for (WormholeEventTrack tracker : trackers) {
                tracker.track(event);
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> void execute(boolean isAsync, HandlerMetaData handler, WormholeEvent<T> event, String action, WormholeCallback callback) {
        Object result = null;
        Exception e = null;
        try {
            if (isAsync) {
                result = PulsarHolder.getPulsar().postForResult(() -> ReflectUtil.invokeWithWrapperInject(handler.getTarget(), handler.getMethod(),
                        Collections.singletonList(event), WormholeEvent.class, WormholeEvent::getData, WormholeEvent::setData));
            } else {
                result = ReflectUtil.invokeWithWrapperInject(handler.getTarget(), handler.getMethod(),
                        Collections.singletonList(event), WormholeEvent.class, WormholeEvent::getData, WormholeEvent::setData);
            }
        } catch (Exception ex) {
            e = ex;
        }

        if (callback != null) {
            if (isAsync && result != null) {
                try {
                    callback.callback(event, action, ((Future) result).get(), e);
                } catch (Exception exception) {
                    log.error("Wormhole get async result error with msg: {}", exception.getMessage(), exception);
                }
            } else {
                callback.callback(event, action, result, e);
            }
        }
    }
}
