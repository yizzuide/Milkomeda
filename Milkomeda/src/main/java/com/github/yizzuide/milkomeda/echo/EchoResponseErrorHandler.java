package com.github.yizzuide.milkomeda.echo;

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

    private ResponseErrorHandler errorHandler = new DefaultResponseErrorHandler();

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
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
