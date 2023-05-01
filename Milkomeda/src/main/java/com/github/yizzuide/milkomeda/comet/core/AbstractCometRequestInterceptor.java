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

import com.github.yizzuide.milkomeda.universe.parser.url.URLPathMatcher;
import com.github.yizzuide.milkomeda.util.RecognizeUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

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
public abstract class AbstractCometRequestInterceptor implements CometRequestInterceptor {

    @Getter
    @Autowired
    private CometProperties cometProperties;

    @Setter @Getter
    private int order;

    @Override
    public String readRequest(HttpServletRequest request, String formName, String formValue, String body) {
        String originalValue = formValue == null ? body : formValue;
        if (originalValue == null) {
            return originalValue;
        }
        CometProperties.RequestInterceptor requestInterceptor = cometProperties.getRequestInterceptors().get(interceptorName());
        if (!CollectionUtils.isEmpty(requestInterceptor.getExcludeUrls())) {
            if (URLPathMatcher.match(requestInterceptor.getExcludeUrls(), request.getRequestURI())) {
                return originalValue;
            }
        }
        if (!URLPathMatcher.match(requestInterceptor.getIncludeUrls(), request.getRequestURI())) {
            return originalValue;
        }
        return doReadRequest(request, formName, formValue, body);
    }

    /**
     * Hook method for get bean name of this interceptor.
     * @return bean name
     */
    protected String interceptorName() {
        return RecognizeUtil.getBeanName(this.getClass());
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
