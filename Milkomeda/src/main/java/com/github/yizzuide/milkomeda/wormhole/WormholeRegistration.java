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

package com.github.yizzuide.milkomeda.wormhole;

import com.github.yizzuide.milkomeda.universe.context.SpringContext;
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
 * <br />
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
        actionMap = SpringContext.getHandlerMetaData(WormholeEventHandler.class, WormholeAction.class, (annotation, handlerAnnotation, metaData) -> {
            WormholeAction wormholeAction = (WormholeAction) annotation;
            boolean isAsyncPresentOn = metaData.getMethod().isAnnotationPresent(Async.class);
            Map<String, Object> attrs = new HashMap<>(4);
            attrs.put(ATTR_HANG_TYPE, wormholeAction.transactionHang());
            attrs.put(ATTR_ASYNC, isAsyncPresentOn);
            metaData.setAttributes(attrs);
            return wormholeAction.value();
        }, false);

        // 领域事件跟踪处理器
        List<WormholeEventTrack<?>> trackers = SpringContext.getTypeHandlers(WormholeEventTracker.class);
        if (!CollectionUtils.isEmpty(trackers)) {
            eventBus.setTrackers(trackers);
        }
    }

    static Map<String, List<HandlerMetaData>> getActionMap() {
        return actionMap;
    }
}
