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

package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.metadata.BeanIds;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPathMatcher;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPlaceholderParser;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPlaceholderResolver;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlResponseOutput;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import com.github.yizzuide.milkomeda.util.Strings;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.github.yizzuide.milkomeda.particle.ParticleProperties.Limiter.RESPONSE_CONTENT;
import static com.github.yizzuide.milkomeda.particle.ParticleProperties.Limiter.RESPONSE_CONTENT_TYPE;

/**
 * ParticleFilter
 * 限制器过滤器
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.12.10
 * Create at 2020/04/08 11:41
 */
public class ParticleFilter implements Filter {

    @Autowired
    private ParticleProperties particleProperties;

    @Autowired(required = false) @Qualifier(BeanIds.PARTICLE_RESOLVER)
    private URLPlaceholderResolver particleURLPlaceholderResolver;

    // 占位解析器
    private URLPlaceholderParser urlPlaceholderParser;

    // 跳过拦截
    private boolean skip = false;

    @PostConstruct
    public void init() {
        List<ParticleProperties.Limiter> limiters = particleProperties.getLimiters();
        if (CollectionUtils.isEmpty(limiters)) {
            skip = true;
            return;
        }
        urlPlaceholderParser = new URLPlaceholderParser();
        urlPlaceholderParser.setCustomURLPlaceholderResolver(particleURLPlaceholderResolver);
        for (ParticleProperties.Limiter limiter : limiters) {
            if (Strings.isEmpty(limiter.getKeyTpl())) {
                continue;
            }
            limiter.setCacheKeys(urlPlaceholderParser.grabPlaceHolders(limiter.getKeyTpl()));
        }
    }

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (skip || (!CollectionUtils.isEmpty(particleProperties.getExcludeUrls()) && URLPathMatcher.match(particleProperties.getExcludeUrls(), httpServletRequest.getRequestURI())) ||
                (!CollectionUtils.isEmpty(particleProperties.getIncludeUrls()) && !URLPathMatcher.match(particleProperties.getIncludeUrls(), httpServletRequest.getRequestURI()))) {
            chain.doFilter(request, response);
            return;
        }

        boolean matchFlag = false;
        List<ParticleProperties.Limiter> urlLimiters = particleProperties.getLimiters();
        if (CollectionUtils.isEmpty(urlLimiters)) {
            chain.doFilter(request, response);
            return;
        }
        for (ParticleProperties.Limiter limiter : urlLimiters) {
            // 忽略需排除的URL
            if (!CollectionUtils.isEmpty(limiter.getExcludeUrls()) && URLPathMatcher.match(limiter.getExcludeUrls(), httpServletRequest.getRequestURI())) {
                continue;
            }
            if (CollectionUtils.isEmpty(limiter.getIncludeUrls())) {
                continue;
            }
            if (!URLPathMatcher.match(limiter.getIncludeUrls(), httpServletRequest.getRequestURI())) {
                continue;
            }
            // 设置匹配标识
            matchFlag = true;
            String key = urlPlaceholderParser.parse(limiter.getKeyTpl(), httpServletRequest, null, null, limiter.getCacheKeys());
            Map<String, Object> returnData = limiter.getLimitHandler().limit(key, particle -> {
                if (particle.isLimited()) {
                    Map<String, Object> result = new HashMap<>(8);
                    Map<String, Object> responseInfo;
                    ParticleProperties.Limiter selectedLimiter = limiter;
                    // 如果是组合类型，查找具体类型
                    if (limiter.getType() == LimiterType.BARRIER) {
                        BarrierLimiter barrierLimitHandler = (BarrierLimiter) limiter.getLimitHandler();
                        selectedLimiter = LimiterConfigSelector.barrierSelect(particle.getType(), barrierLimitHandler.getChain(), particleProperties);
                        // 如里子类型没提供响应，那就指定自己
                        if (selectedLimiter == null || selectedLimiter.getResponse() == null) {
                            selectedLimiter  = limiter;
                        }
                    }
                    responseInfo = selectedLimiter.getResponse();
                    // 如果具体类型没有提供，查找全局的响应
                    if (responseInfo == null) {
                        responseInfo = particleProperties.getResponse();
                    }
                    // 如果没有响应，返回默认响应码
                    if (responseInfo == null || responseInfo.get(YmlResponseOutput.STATUS) == null) {
                        result.put(YmlResponseOutput.STATUS, 416);
                        return result;
                    }
                    int status = Integer.parseInt(responseInfo.get(YmlResponseOutput.STATUS).toString());
                    result.put(YmlResponseOutput.STATUS, status);
                    if (MediaType.APPLICATION_JSON_VALUE.equals(selectedLimiter.getResponseContentType())) {
                        YmlResponseOutput.output(responseInfo, result, null, null, false);
                    } else {
                        result.put(RESPONSE_CONTENT, responseInfo.get(RESPONSE_CONTENT));
                    }
                    result.put(RESPONSE_CONTENT_TYPE, selectedLimiter.getResponseContentType());
                    return result;
                }
                chain.doFilter(request, response);
                return null;
            });

            if (returnData != null) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.setStatus(Integer.parseInt(returnData.get(YmlResponseOutput.STATUS).toString()));
                returnData.remove(YmlResponseOutput.STATUS);
                httpServletResponse.setCharacterEncoding("UTF-8");
                String contentType = String.valueOf(returnData.get(RESPONSE_CONTENT_TYPE));
                String data;
                if (MediaType.APPLICATION_JSON_VALUE.equals(contentType)) {
                    returnData.remove(RESPONSE_CONTENT_TYPE);
                    data = JSONUtil.serialize(returnData);
                } else {
                    data = String.valueOf(returnData.get(RESPONSE_CONTENT));
                }
                httpServletResponse.setContentType(contentType);
                PrintWriter writer = httpServletResponse.getWriter();
                writer.println(data);
                writer.flush();
                return;
            }

            // 只支持一个限制器，可通过排序来确定，限制器链可以使用Barrier类型
            break;
        }

        // 放过不需要限制的请求
        if (!matchFlag) {
            chain.doFilter(request, response);
        }
    }
}
