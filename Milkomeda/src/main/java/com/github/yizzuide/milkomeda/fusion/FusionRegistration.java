package com.github.yizzuide.milkomeda.fusion;

import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FusionRegistration
 *
 * @author yizzuide
 * @since 3.3.0
 * @version 3.3.2
 * Create at 2020/05/05 16:23
 */
@Slf4j
public class FusionRegistration {

    private static Map<String, List<HandlerMetaData>> actionMap = new HashMap<>();

    @Autowired
    private FusionAspect fusionAspect;

    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (event.getApplicationContext().getParent() == null) {
            return;
        }
        actionMap = AopContextHolder.getHandlerMetaData(FusionHandler.class, FusionAction.class, (annotation, handlerAnnotation, metaData) -> {
            FusionAction fusionAction = (FusionAction) annotation;
            return fusionAction.value();
        }, false);

        if (fusionAspect.getConverter() == null) {
            fusionAspect.setConverter((tag, returnObj, error) -> {
                List<HandlerMetaData> handlerMetaDataList = actionMap.get(tag);
                if (CollectionUtils.isEmpty(handlerMetaDataList)) {
                    return returnObj;
                }
                HandlerMetaData handlerMetaData = handlerMetaDataList.get(0);
                FusionMetaData<?> fusionMetaData = FusionMetaData.builder().returnData(returnObj).error(returnObj == null).msg(error).build();
                try {
                    return handlerMetaData.getMethod().invoke(handlerMetaData.getTarget(), fusionMetaData);
                } catch (Exception e) {
                    log.error("Fusion invoke error with msg: {}", e.getMessage(), e);
                }
                return returnObj;
            });
        }

    }
}
