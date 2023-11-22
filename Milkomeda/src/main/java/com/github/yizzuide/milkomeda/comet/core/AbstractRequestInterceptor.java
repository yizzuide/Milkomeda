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

package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.universe.extend.web.handler.HotHttpHandlerProperty;
import com.github.yizzuide.milkomeda.universe.extend.web.handler.NamedHandler;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;

/**
 * The abstract request interceptor which provide url filter.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/05/01 18:35
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public abstract class AbstractRequestInterceptor implements CometRequestInterceptor {

    @Setter @Getter
    private int order;

    @Getter
    @Autowired
    private CometProperties cometProperties;

    @Override
    public String readRequest(HttpServletRequest request, String formName, String formValue, String body) {
        String originalValue = formValue == null ? body : formValue;
        if (originalValue == null) {
            return originalValue;
        }
        HotHttpHandlerProperty requestInterceptor = cometProperties.getRequestInterceptors().get(handlerName());
        if (!NamedHandler.canHandle(request, requestInterceptor)) {
            return originalValue;
        }
        return doReadRequest(request, formName, formValue, body);
    }

    /**
     * Hook method for read request and change parameter values.
     * @param request   HttpServletRequest
     * @param formName  form field name
     * @param formValue form field value
     * @param body  request body
     * @return parameter value
     */
    protected abstract String doReadRequest(HttpServletRequest request, String formName, String formValue, String body);


}
