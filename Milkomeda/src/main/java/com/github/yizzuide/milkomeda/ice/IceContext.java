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

package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.context.AopContextHolder;
import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * IceContext
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 3.0.7
 * Create at 2019/11/17 18:40
 */
@Slf4j
public class IceContext implements ApplicationListener<ContextRefreshedEvent> {

    private static Map<String, List<HandlerMetaData>> topicMap = new HashMap<>();

    private static Map<String, List<HandlerMetaData>> topicTtrMap = new HashMap<>();

    private static Map<String, List<HandlerMetaData>> topicTtrOverloadMap = new HashMap<>();


    @Autowired
    private IceProperties props;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        topicMap = AopContextHolder.getHandlerMetaData(IceHandler.class, IceListener.class, (annotation, handlerAnnotation, metaData) -> {
                    IceListener iceListener = (IceListener) annotation;
                    return iceListener.value();
                }, !props.isMultiTopicListenerPerHandler());
        topicTtrMap = AopContextHolder.getHandlerMetaData(IceHandler.class, IceTtrListener.class, (annotation, handlerAnnotation, metaData) -> {
            IceTtrListener iceTtrListener = (IceTtrListener) annotation;
            return iceTtrListener.value();
        }, !props.isMultiTopicListenerPerHandler());
        topicTtrOverloadMap = AopContextHolder.getHandlerMetaData(IceHandler.class, IceTtrOverloadListener.class, (annotation, handlerAnnotation, metaData) -> {
            IceTtrOverloadListener iceTtrOverloadListener = (IceTtrOverloadListener) annotation;
            return iceTtrOverloadListener.value();
        }, !props.isMultiTopicListenerPerHandler());
    }

    static Map<String, List<HandlerMetaData>> getTopicMap() {
        return topicMap;
    }

    static Map<String, List<HandlerMetaData>> getTopicTtrMap() {
        return topicTtrMap;
    }

    static Map<String, List<HandlerMetaData>> getTopicTtrOverloadMap() {
        return topicTtrOverloadMap;
    }
}
