package com.github.yizzuide.milkomeda.comet.collector;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * CometCollectorResponseBodyAdvice
 * 控制器响应切面
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/29 11:30
 */
@ControllerAdvice
public class CometCollectorResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @SuppressWarnings("all")
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                        .getRequest().setAttribute("comet.collect.body", body);
        return body;
    }
}
