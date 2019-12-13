package com.github.yizzuide.milkomeda.comet;

import com.github.yizzuide.milkomeda.util.JSONUtil;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;
import java.util.Map;

/**
 * CometParamResolver
 * 支持CometParam自定义参数处理器
 *
 * @see org.springframework.web.method.support.HandlerMethodArgumentResolver
 * @see org.springframework.web.method.annotation.ModelAttributeMethodProcessor
 *
 * @author yizzuide
 * @since 2.0.0
 * Create at 2019/12/12 22:08
 */
public class CometParamResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(CometParam.class);
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        // methodParameter.getParameterAnnotation(CometParam.class);
        String params = CometAspect.resolveRequestParams(true);
        CometAspect.resolveThreadLocal.set(params);
        Class<?> parameterType = methodParameter.getParameterType();
        // Map类型
        if (Map.class.isAssignableFrom(parameterType)) {
            return JSONUtil.parseMap(params, String.class, Object.class);
        } else if (List.class.isAssignableFrom(parameterType)) {
            return JSONUtil.parseList(params, Map.class);
        }
        // 自定义对象类型
        return JSONUtil.parse(params, parameterType);
    }
}
