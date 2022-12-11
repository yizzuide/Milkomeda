/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.fusion;

import com.github.yizzuide.milkomeda.universe.context.SpringContext;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
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
 * <br>
 * Create at 2020/05/05 16:23
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Slf4j
public class FusionRegistration {

    private static Map<String, List<HandlerMetaData>> actionMap = new HashMap<>();

    @Autowired
    private FusionAspect fusionAspect;

    @EventListener
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        actionMap = SpringContext.getHandlerMetaData(FusionHandler.class, FusionAction.class, (annotation, handlerAnnotation, metaData) -> {
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
