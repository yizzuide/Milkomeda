package com.github.yizzuide.milkomeda.hydrogen.uniform;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * UniformException
 * 统一异常
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/03 17:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UniformException extends RuntimeException {
    private static final long serialVersionUID = -5573748402659884460L;

    private int code;

    public UniformException(UniformExceptionData uniformExceptionData, String message) {
        super(message);
        this.code = uniformExceptionData.getCode();
    }

    public UniformException(UniformExceptionData uniformExceptionData, String message, Throwable t) {
        super(message, t);
        this.code = uniformExceptionData.getCode();
    }

}
