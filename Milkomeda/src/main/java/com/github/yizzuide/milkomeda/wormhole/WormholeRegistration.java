package com.github.yizzuide.milkomeda.wormhole;

import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
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
 * Create at 2020/05/05 14:15
 */
public class WormholeRegistration {

    private static Map<String, List<HandlerMetaData>> actionMap = new HashMap<>();

    @Autowired
    private WormholeEventBus eventBus;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            return;
        }
        actionMap = AopContextHolder.getHandlerMetaData(WormholeEventHandler.class, WormholeAction.class, (annotation, handlerAnnotation, metaData) -> {
            WormholeAction wormholeAction = (WormholeAction) annotation;
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
