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

package com.github.yizzuide.milkomeda.echo;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClientException;

import java.io.IOException;

/**
 * EchoResponseErrorHandler
 *
 * @author yizzuide
 * @since 1.13.4
 * @since 1.13.9
 * Create at 2019/10/24 14:09
 */
@Slf4j
public class EchoResponseErrorHandler implements ResponseErrorHandler {

    private final ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

    @Override
    public void handleError(@NonNull ClientHttpResponse response) throws IOException {
        try {
            errorHandler.handleError(response);
        } catch (RestClientException e) {
            if (e instanceof HttpStatusCodeException) {
                HttpStatusCodeException ce = (HttpStatusCodeException) e;
                log.error("Echo request with error code: {}, msg:{}, body:{}", ce.getStatusCode().value(), ce.getMessage(), ce.getResponseBodyAsString());
                throw new EchoException(ce.getStatusCode().value(), ce.getMessage(), ce.getResponseBodyAsString());
            }
            throw new EchoException(ErrorCode.VENDOR_REQUEST_ERROR, e.getMessage());
        }
    }

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return errorHandler.hasError(response);
    }
}
