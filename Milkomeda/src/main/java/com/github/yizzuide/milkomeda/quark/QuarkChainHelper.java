/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.EventHandlerGroup;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A helper class to make chain with event handlers.
 *
 * @since 3.20.0
 * @author yizzuide
 * Create at 2024/06/15 20:57
 */
public class QuarkChainHelper {

    private static Map<String, List<QuarkEventHandler<?>>> topicEventHandlerMap;

    private static final Map<String, List<Object>> topicHandlerLinkMap = new HashMap<>();

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<QuarkEventHandler<?>> invoke(Disruptor<QuarkEvent<Object>> disruptor, String topic) {
        List<QuarkEventHandler<?>> quarkEventHandlers = topicEventHandlerMap.get(topic);
        if (disruptor == null) {
            return quarkEventHandlers;
        }
        if (topicHandlerLinkMap.isEmpty()) {
            // handlers execute concurrently
            disruptor.handleEventsWith(quarkEventHandlers.toArray(new EventHandler[]{}));
            return quarkEventHandlers;
        }
        List<Object> handlers = topicHandlerLinkMap.get(topic);
        EventHandlerGroup<?> eventHandlerGroup = null;
        for (Object handler : handlers) {
            if (eventHandlerGroup == null) {
                if (handler instanceof Collection<?>) {
                    Collection<?> handlerList = (Collection<?>) handler;
                    eventHandlerGroup = disruptor.handleEventsWith(handlerList.toArray(new EventHandler[]{}));
                } else {
                    eventHandlerGroup = disruptor.handleEventsWith((EventHandler) handler);
                }
                continue;
            }

            // this handler executes after upper handlers
            // .then(h3);
            // h4 and h5 execute concurrently after h3
            // .after(h3).handleEventsWith(h4, h5);
            if (handler instanceof Collection<?>) {
                Collection<?> handlerList = (Collection<?>) handler;
                eventHandlerGroup.handleEventsWith(handlerList.toArray(new EventHandler[]{}));
            } else {
                eventHandlerGroup.then((EventHandler) handler);
            }
        }
        return quarkEventHandlers;
    }

    public static void setTopicEventHandlers(Map<String, List<QuarkEventHandler<?>>> topicEventHandlerMap,
                                             Map<String, QuarkEventHandler<?>> namedEventHandlerMap,
                                             Map<String, String> chainMap) {
        QuarkChainHelper.topicEventHandlerMap = topicEventHandlerMap;
        chainMap.forEach((key, value) -> {
            String[] chains = value.split("\\s->\\s");
            if (chains.length == 1) {
                return;
            }

            topicHandlerLinkMap.put(key, new ArrayList<>(chains.length));
            Arrays.stream(chains).forEach(chain -> {
                if (chain.contains(",")) {
                    String[] chainNames = chain.split(",");
                    List<? extends QuarkEventHandler<?>> list = Arrays.stream(chainNames)
                            .map(String::trim)
                            .map(namedEventHandlerMap::get)
                            .collect(Collectors.toList());
                    topicHandlerLinkMap.get(key).add(list);
                    return;
                }
                topicHandlerLinkMap.get(key).add(namedEventHandlerMap.get(chain));
            });
        });
    }
}
