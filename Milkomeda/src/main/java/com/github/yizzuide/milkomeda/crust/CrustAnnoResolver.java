/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.crust;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PathPatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The resolver for {@link CrustAnon} annotation.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/08/06 14:34
 */
public class CrustAnnoResolver {
    /**
     * Resolve request method URL with {@link CrustAnon} annotation
     * @param applicationContextHolder ApplicationContextHolder
     * @return method URLs
     */
    public static Set<String> resolve(ApplicationContextHolder applicationContextHolder) {
        // 标记匿名访问
        // Find URL method map
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = applicationContextHolder.getApplicationContext()
                .getBean(com.github.yizzuide.milkomeda.universe.metadata.BeanIds.REQUEST_MAPPING_HANDLER_MAPPING,
                        RequestMappingHandlerMapping.class).getHandlerMethods();
        Set<String> anonUrls = new HashSet<>();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> infoEntry : handlerMethodMap.entrySet()) {
            HandlerMethod handlerMethod = infoEntry.getValue();
            // Has `CrustAnon` annotation on Method？
            CrustAnon crustAnon = handlerMethod.getMethodAnnotation(CrustAnon.class);
            if (null != crustAnon) {
                Collection<String> requestPatterns = null;
                PatternsRequestCondition patternsCondition = infoEntry.getKey().getPatternsCondition();
                if (patternsCondition != null) {
                    requestPatterns = patternsCondition.getPatterns();
                }
                if (requestPatterns == null) {
                    PathPatternsRequestCondition pathPatternsCondition = infoEntry.getKey().getPathPatternsCondition();
                    if (pathPatternsCondition != null) {
                        requestPatterns = pathPatternsCondition.getPatterns().stream().map(PathPattern::getPatternString).collect(Collectors.toSet());
                    }
                }
                if (requestPatterns != null) {
                    anonUrls.addAll(requestPatterns);
                }
            }
        }
        return anonUrls;
    }
}
