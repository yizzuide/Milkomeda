package com.github.yizzuide.milkomeda.wormhole;

import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DomainRegistration
 * 领域事件注册
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.11.1
 * Create at 2020/05/05 14:15
 */
public class WormholeRegistration {

    private static Map<String, List<HandlerMetaData>> actionMap = new HashMap<>();

    /**
     * 事件挂载类型
     */
    public static final String ATTR_HANG_TYPE = "hangType";

    /**
     * 调用处理异步方式的方法属性名
     */
    public static final String ATTR_ASYNC = "async";

    @Autowired
    private WormholeEventBus eventBus;

    @EventListener
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        actionMap = AopContextHolder.getHandlerMetaData(WormholeEventHandler.class, WormholeAction.class, (annotation, handlerAnnotation, metaData) -> {
            WormholeAction wormholeAction = (WormholeAction) annotation;
            boolean isAsyncPresentOn = metaData.getMethod().isAnnotationPresent(Async.class);
            Map<String, Object> attrs = new HashMap<>(4);
            attrs.put(ATTR_HANG_TYPE, wormholeAction.transactionHang());
            attrs.put(ATTR_ASYNC, isAsyncPresentOn);
            metaData.setAttributes(attrs);
            return wormholeAction.value();
        }, false);

        // 领域事件跟踪处理器
        List<WormholeEventTrack> trackers = AopContextHolder.getTypeHandlers(WormholeEventTracker.class);
        if (!CollectionUtils.isEmpty(trackers)) {
            eventBus.setTrackers(trackers);
        }
    }

    static Map<String, List<HandlerMetaData>> getActionMap() {
        return actionMap;
    }
}
