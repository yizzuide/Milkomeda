package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * IceContext
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/17 18:40
 */
@Slf4j
public class IceContext implements ApplicationListener<ContextRefreshedEvent> {

    private static Map<String, List<HandlerMetaData>> topicMap;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        topicMap = AopContextHolder.getHandlerMetaData(IceHandler.class, IceListener.class, annotation -> {
                    IceListener iceListener = (IceListener) annotation;
                    return StringUtils.isEmpty(iceListener.topic()) ? iceListener.value() : iceListener.topic();
                }, false, true);
    }

    static Map<String, List<HandlerMetaData>> getTopicMap() {
        return topicMap;
    }
}
