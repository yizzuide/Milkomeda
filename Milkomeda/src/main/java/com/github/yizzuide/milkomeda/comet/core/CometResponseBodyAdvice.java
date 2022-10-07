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

package com.github.yizzuide.milkomeda.comet.core;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * CometCollectorResponseBodyAdvice
 * 控制器响应切面
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.0.1
 * @see WebMvcConfigurationSupport#handlerExceptionResolver(org.springframework.web.accept.ContentNegotiationManager)
 * #see RequestMappingHandlerAdapter#getDefaultReturnValueHandlers()
 * @see AbstractMessageConverterMethodArgumentResolver#AbstractMessageConverterMethodArgumentResolver(java.util.List, java.util.List)
 * #see RequestResponseBodyAdviceChain#getAdviceByType(java.util.List, java.lang.Class)
 * <br />
 * Create at 2020/03/29 11:30
 */
//@ControllerAdvice // 这种方式默认就会扫描并加载到Ioc，不好动态控制是否加载，但好处是外部API对未来版本的兼容性强
public class CometResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    public static final String REQUEST_ATTRIBUTE_BODY = "comet.response.body";

    @Override
    public boolean supports(@NonNull MethodParameter returnType, @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, @NonNull MethodParameter returnType, @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            ((ServletRequestAttributes) requestAttributes).getRequest().setAttribute(REQUEST_ATTRIBUTE_BODY, body);
        }
        return body;
    }
}
