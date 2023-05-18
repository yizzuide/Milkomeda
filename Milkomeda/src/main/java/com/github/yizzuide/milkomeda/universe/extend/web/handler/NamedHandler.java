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

package com.github.yizzuide.milkomeda.universe.extend.web.handler;

import com.github.yizzuide.milkomeda.universe.extend.annotation.Alias;
import com.github.yizzuide.milkomeda.universe.extend.annotation.AliasWrapper;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPathMatcher;
import com.github.yizzuide.milkomeda.util.RecognizeUtil;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import org.springframework.core.OrderComparator;
import org.springframework.core.PriorityOrdered;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This handler interface provide named and ordered in list. it always used with {@link Alias} and {@link AliasWrapper}.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/05/05 22:36
 */
public interface NamedHandler extends PriorityOrdered {
    /**
     * Set order in list.
     * @param order the order used sort in list
     */
    default void setOrder(int order) {}

    /**
     * Get order in list.
     * @return the order used sort in list
     */
    @Override
    default int getOrder() {
        return 0;
    }

    /**
     * Set handler name.
     * @return handler name.
     */
    default String handlerName() {
        Class<?> handlerClass = this.getClass();
        if (handlerClass.isAnnotationPresent(Alias.class)) {
            return handlerClass.getAnnotation(Alias.class).value();
        }
        return RecognizeUtil.getBeanName(this.getClass());
    }

    /**
     * Convert bean map to {@link AliasWrapper} map.
     * @param beanMap bean map that bean name bind bean instance
     * @return  {@link AliasWrapper} map
     * @param <T> handler class type
     */
    static <T extends NamedHandler> Map<String, AliasWrapper<T>> mapFrom(Map<String, T> beanMap) {
        return beanMap.entrySet().stream()
                .map(e -> new AbstractMap.SimpleEntry<>(e.getValue().handlerName(), new AliasWrapper<>(e.getValue().handlerName(), e.getKey(), e.getValue())))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Build sorted handler list from bean map.
     * @param beanMap   bean map that bean name bind bean instance
     * @param get   get handler property from handler name
     * @return  handler list
     * @param <T>   handler class type
     */
    static <T extends NamedHandler> List<T> sortedList(Map<String, T> beanMap, Function<String, ? extends HandlerProperty> get) {
        Map<String, AliasWrapper<T>> handlerAliasMap = mapFrom(beanMap);
        List<T> handlerList = new ArrayList<>();
        Set<String> interceptorNames = handlerAliasMap.keySet();
        for (String interceptorName : interceptorNames) {
            HandlerProperty handlerProperty = get.apply(interceptorName);
            T handler = handlerAliasMap.get(interceptorName).getBean();
            // 是否为热开启处理器
            if (handlerProperty instanceof HotHandlerProperty) {
                if (!((HotHandlerProperty) handlerProperty).isEnable()) {
                    continue;
                }
            }
            handler.setOrder(handlerProperty.getOrder());
            if (handlerProperty.getProps() != null) {
                ReflectUtil.setField(handler, handlerProperty.getProps());
            }
            handlerList.add(handler);
        }
        if (!CollectionUtils.isEmpty(handlerList)) {
            handlerList = handlerList.stream()
                    .sorted(OrderComparator.INSTANCE.withSourceProvider(itr -> itr)).collect(Collectors.toList());
        }
        return handlerList;
    }

    /**
     * Check can handle with request URL.
     * @param request   http request
     * @param handlerProperty handler config property
     * @return  true if handle success
     */
    static boolean canHandle(HttpServletRequest request, HttpHandlerProperty handlerProperty) {
        return canHandle(request, handlerProperty.getIncludeUrls(), handlerProperty.getExcludeUrls());
    }

    /**
     * Check can handle with request URL.
     * @param request   http request
     * @param includeUrls   need include URL
     * @param excludeUrls   must ignore URL
     * @return  true if handle success
     */
    static boolean canHandle(HttpServletRequest request, List<String> includeUrls, List<String> excludeUrls) {
        if (request == null) {
            return false;
        }
        String url = request.getRequestURI();
        if (CollectionUtils.isEmpty(includeUrls)) {
            return false;
        }
        if (!CollectionUtils.isEmpty(excludeUrls)) {
            if (URLPathMatcher.match(excludeUrls, url)) {
                return false;
            }
        }
        return URLPathMatcher.match(includeUrls, url);
    }
}
