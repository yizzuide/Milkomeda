/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.quark;

import com.lmax.disruptor.ExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Quark config.
 *
 * @since 3.15.0
 * @version 4.0.0
 * @author yizzuide
 * Create at 2023/08/19 10:52
 */
@EnableScheduling
@Configuration
@EnableConfigurationProperties(QuarkProperties.class)
public class QuarkConfig implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private QuarkProperties props;

    @Autowired
    private List<QuarkEventHandler<?>> eventHandlerList;

    @Autowired(required = false)
    private List<ExceptionHandler<?>> exceptionHandlerList;

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent event) {
        if (Quarks.getBufferSize() != null) {
            return;
        }
        Quarks.setWarningPercent(props.getWarningPercent());
        QuarkProperties.Pool pool = props.getPool();
        ThreadPoolExecutor executor = new ThreadPoolExecutor(pool.getCore(), pool.getMaximum(),
                pool.getKeepAliveTime().toMillis(), TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(pool.getQueueSize()),
                new ThreadPoolExecutor.DiscardPolicy());
        Quarks.setExecutor(executor);
        Quarks.setBufferSize(props.getBufferSize());
        if (!CollectionUtils.isEmpty(eventHandlerList)) {
            Map<String, List<QuarkEventHandler<?>>> topicEventHandlerMap = new HashMap<>();
            eventHandlerList.stream().filter(ha -> {
                Quark quark = AnnotationUtils.findAnnotation(ha.getClass(), Quark.class);
                if (quark == null) {
                    return false;
                }
                return quark.usedEventHandler();
            }).forEach(ha -> {
                Quark quark = AnnotationUtils.findAnnotation(ha.getClass(), Quark.class);
                assert quark != null;
                topicEventHandlerMap.computeIfAbsent(quark.topic(), k -> new ArrayList<>()).add(ha);
            });
            Quarks.setEventHandlerList(topicEventHandlerMap);

            Map<String, ExceptionHandler<?>> topicExceptionHandlerMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(exceptionHandlerList)) {
                exceptionHandlerList.stream().filter(eh -> {
                    Quark quark = AnnotationUtils.findAnnotation(eh.getClass(), Quark.class);
                    if (quark == null) {
                        return false;
                    }
                    return quark.usedExceptionHandler();
                }).forEach(eh -> {
                    Quark quark = AnnotationUtils.findAnnotation(eh.getClass(), Quark.class);
                    assert quark != null;
                    topicExceptionHandlerMap.put(quark.topic(), eh);
                });
                Quarks.setExceptionHandlerList(topicExceptionHandlerMap);
            }
        }
    }
}
