package com.github.yizzuide.milkomeda.echo;

import lombok.Getter;
import org.springframework.web.client.RestClientException;

/**
 * EchoException
 *
 * @author yizzuide
 * @since 1.13.0
 * Create at 2019/09/21 17:17
 */
public class EchoException extends RestClientException {
    private static final long serialVersionUID = -1012633504047012324L;
    @Getter
    private int code;
    @Getter
    private String body;

    public EchoException(String message) {
        super(message);
    }

    public EchoException(int code, String message) {
        super(message);
        this.code = code;
    }

    public EchoException(int code, String message, String body) {
        this(code, message);
        this.body = body;
    }
}
