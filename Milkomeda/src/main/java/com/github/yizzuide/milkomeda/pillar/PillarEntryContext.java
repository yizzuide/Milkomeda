package com.github.yizzuide.milkomeda.pillar;

import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * PillarContext
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/02 17:15
 */
public class PillarEntryContext implements ApplicationListener<ContextRefreshedEvent> {

    public static final String ATTR_CODE = "code";

    private static Map<String, List<HandlerMetaData>> pillarEntryMap = new HashMap<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            return;
        }
        pillarEntryMap = AopContextHolder.getHandlerMetaData(PillarEntryHandler.class, PillarEntryPoint.class, (annotation, handlerAnnotation, metaData) -> {
            PillarEntryHandler pillarEntryHandler = (PillarEntryHandler) handlerAnnotation;
            PillarEntryPoint pillarEntryPoint = (PillarEntryPoint) annotation;
            String tag = pillarEntryHandler.tag();
            if (StringUtils.isEmpty(tag)) {
                tag = pillarEntryPoint.tag();
            }
            // 设置其它属性方法的值
            Map<String, Object> attrs = new HashMap<>(2);
            attrs.put(ATTR_CODE, pillarEntryPoint.code());
            metaData.setAttributes(attrs);
            return tag;
        }, false);
    }

    static Map<String, List<HandlerMetaData>> getPillarEntryMap() {
        return pillarEntryMap;
    }
}
