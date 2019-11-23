package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IceContext
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 1.16.0
 * Create at 2019/11/17 18:40
 */
@Slf4j
public class IceContext implements ApplicationListener<ContextRefreshedEvent> {

    private static Map<String, List<HandlerMetaData>> topicMap = new HashMap<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        topicMap = AopContextHolder.getHandlerMetaData(IceHandler.class, IceListener.class, annotation -> {
                    IceListener iceListener = (IceListener) annotation;
                    return iceListener.value();
                }, false, true);
    }

    static Map<String, List<HandlerMetaData>> getTopicMap() {
        return topicMap;
    }
}
