package com.github.yizzuide.milkomeda.echo;

/**
 * EchoException
 *
 * @author yizzuide
 * @since 1.13.0
 * Create at 2019/09/21 17:17
 */
public class EchoException extends Exception {
    private int code;

    public EchoException(String message) {
        super(message);
    }

    public EchoException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
