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

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.extend.web.handler.HotHttpHandlerProperty;
import com.github.yizzuide.milkomeda.universe.extend.web.handler.NamedHandler;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;

import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;

/**
 * The abstract response interceptor which provide url filter and write the changed response.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/05/06 02:47
 */
@Getter
@Slf4j
public abstract class AbstractResponseInterceptor implements CometResponseInterceptor {

    @Setter
    private int order;

    @Autowired
    private CometProperties cometProperties;

    @Override
    public boolean writeToResponse(FastByteArrayOutputStream outputStream, HttpServletResponse wrapperResponse, HttpServletResponse rawResponse, Object body) {
        HotHttpHandlerProperty responseInterceptor = cometProperties.getResponseInterceptors().get(handlerName());
        if (!NamedHandler.canHandle(WebContext.getRequest(), responseInterceptor)) {
            return false;
        }
        try {
            Object result = doResponse(rawResponse, body);
            if (result == null) {
                return false;
            }
            String content = JSONUtil.serialize(result);
            // reset content and length
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            wrapperResponse.resetBuffer();
            wrapperResponse.setContentLength(bytes.length);
            outputStream.write(bytes);
            rawResponse.setContentLength(outputStream.size());
            // write to response
            outputStream.writeTo(rawResponse.getOutputStream());
        } catch (Exception e) {
            log.error("Comet response interceptor error with msg: {}", e.getMessage(), e);
            return false;
        }
        return true;
    }

    protected abstract Object doResponse(HttpServletResponse response, Object body);
}
