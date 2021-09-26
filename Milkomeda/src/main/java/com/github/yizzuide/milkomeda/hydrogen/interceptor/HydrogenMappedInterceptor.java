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

package com.github.yizzuide.milkomeda.hydrogen.interceptor;

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import lombok.Data;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.MappedInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.web.servlet.HandlerMapping.LOOKUP_PATH;

/**
 * HydrogenMappedInterceptor
 * 装饰模式增强的MappedInterceptor
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/31 14:56
 */
@Data
public class HydrogenMappedInterceptor implements HandlerInterceptor, Ordered {

    // 由于MappedInterceptor被final修饰，所以使用继承方式无解
    private MappedInterceptor mappedInterceptor;

    /**
     * 排序
     */
    private int order = 0;

    public HydrogenMappedInterceptor(MappedInterceptor mappedInterceptor) {
        this.mappedInterceptor = mappedInterceptor;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // 由于Spring MVC源码在创建interceptorChain时只识别MappedInterceptor类型才能匹配URL，这里补充进来
        String lookupPath = WebContext.getUrlPathHelper().getLookupPathForRequest(request, LOOKUP_PATH);
        if (mappedInterceptor.matches(lookupPath, WebContext.getMvcPathMatcher())) {
            return mappedInterceptor.preHandle(request, response, handler);
        }
        return true;
    }

    @Override
    public void postHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, ModelAndView modelAndView) throws Exception {
        String lookupPath = WebContext.getUrlPathHelper().getLookupPathForRequest(request, LOOKUP_PATH);
        if (mappedInterceptor.matches(lookupPath, WebContext.getMvcPathMatcher())) {
            mappedInterceptor.postHandle(request, response, handler, modelAndView);
        }
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex) throws Exception {
        mappedInterceptor.afterCompletion(request, response, handler, ex);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof HandlerInterceptor)) {
            return false;
        }
        // 当前装饰类的比较
        if (obj instanceof HydrogenMappedInterceptor) {
            return this.getMappedInterceptor().getInterceptor().getClass() ==
                    ((HydrogenMappedInterceptor) obj).getMappedInterceptor().getInterceptor().getClass();
        }
        // HandlerInterceptor
        return this.getMappedInterceptor().getInterceptor().getClass() == obj.getClass();
    }
}
