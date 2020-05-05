package com.github.yizzuide.milkomeda.wormhole;

import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;

/**
 * WormholeEventBus
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 14:30
 */
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
                Exception e = null;
                Object result = null;
                try {
                    result = ReflectUtil.invokeWithWrapperInject(handler.getTarget(), handler.getMethod(),
                            Collections.singletonList(event), WormholeEvent.class, WormholeEvent::getData, WormholeEvent::setData);
                } catch (Exception ex) {
                    e = ex;
                }

                if (callback != null) {
                    callback.callback(event, action, result, e);
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
}
