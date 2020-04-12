package com.github.yizzuide.milkomeda.comet.collector;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * CometCollectorResponseBodyAdvice
 * 控制器响应切面
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.0.1
 * Create at 2020/03/29 11:30
 */
//@ControllerAdvice // 这种方式默认就会扫描并加载到Ioc，不好动态控制是否加载，但好处是外部API对未来版本的兼容性强
public class CometCollectorResponseBodyAdvice implements ResponseBodyAdvice<Object> {

    public static final String REQUEST_ATTRIBUTE_BODY = "comet.collect.body";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            ((ServletRequestAttributes) requestAttributes).getRequest().setAttribute(REQUEST_ATTRIBUTE_BODY, body);
        }
        return body;
    }
}
