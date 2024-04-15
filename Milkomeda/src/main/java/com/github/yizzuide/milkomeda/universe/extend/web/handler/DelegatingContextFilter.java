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

package com.github.yizzuide.milkomeda.universe.extend.web.handler;

import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformException;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformHandler;
import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DelegatingContextFilter
 * 代理上下文过滤器
 *
 * @see org.springframework.web.filter.RequestContextFilter
 * @see org.springframework.boot.web.servlet.filter.OrderedRequestContextFilter
 * @author yizzuide
 * @since 3.3.1
 * @version 3.12.9
 * <br>
 * Create at 2020/05/06 11:12
 */
@Slf4j
public class DelegatingContextFilter implements Filter {

    @Autowired(required = false)
    private List<AstrolabeHandler> astrolabeHandlers = new ArrayList<>();

    @PostConstruct
    public void init() {
        if (CollectionUtils.isEmpty(astrolabeHandlers)) {
            return;
        }
        // 根据@Order注解排序
        AnnotationAwareOrderComparator.sort(astrolabeHandlers);
        // 根据Order接口排序
        if (astrolabeHandlers.stream().anyMatch(a -> a.getOrder() != 0)) {
            astrolabeHandlers = astrolabeHandlers.stream()
                    .sorted(OrderComparator.INSTANCE.withSourceProvider(ha -> ha)).collect(Collectors.toList());
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        for (AstrolabeHandler astrolabeHandler : astrolabeHandlers) {
            try {
                astrolabeHandler.preHandle(request);
            } catch (UniformException e) {
                UniformHandler.matchStatusToWrite((HttpServletResponse) response, e.getCode(), e);
                return;
            }
        }
        try {
            chain.doFilter(request, response);
        } finally {
            try {
                for (AstrolabeHandler astrolabeHandler : astrolabeHandlers) {
                    astrolabeHandler.postHandle(request, response);
                }
            } catch (Exception e) {
                log.error("astrolabe handler post handle with error msg: {}", e.getMessage(), e);
            }
        }
    }
}
