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

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import org.springframework.core.MethodParameter;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
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
 * @version 3.10.0
 * Create at 2019/12/12 22:08
 */
public class CometParamResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.hasParameterAnnotation(CometParam.class);
    }

    @Override
    public Object resolveArgument(@NonNull MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                  @NonNull NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        // methodParameter.getParameterAnnotation(CometParam.class);
        String params = CometRequestWrapper.resolveRequestParams(WebContext.getRequest(),true);
        if (!StringUtils.hasLength(params)) {
            return null;
        }
        CometAspect.resolveThreadLocal.set(params);
        Class<?> parameterType = methodParameter.getParameterType();
        // Is matched String
        if (String.class.isAssignableFrom(parameterType)) {
            return params;
        }
        // Map
        if (Map.class.isAssignableFrom(parameterType)) {
            Map<String, Object> map = JSONUtil.parseMap(params, String.class, Object.class);
            // 检测是否需要验签
            CometParam cometParam = methodParameter.getParameterAnnotation(CometParam.class);
            if (cometParam == null || cometParam.decrypt() == CometParamDecrypt.class
                    || !CometParamDecrypt.class.isAssignableFrom(cometParam.decrypt())) {
                return map;
            }
            CometParamDecrypt cometParamDecrypt = ApplicationContextHolder.get().getBean(cometParam.decrypt());
            return cometParamDecrypt.decrypt(map);
        }
        // List
        if (List.class.isAssignableFrom(parameterType)) {
            return JSONUtil.parseList(params, Map.class);
        }
        // custom object
        return JSONUtil.parse(params, parameterType);
    }
}
