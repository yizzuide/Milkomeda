package com.github.yizzuide.milkomeda.demo.hydrogen.exception;

import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformExceptionDataAssert;

/**
 * ResponseEnum
 *
 * @author yizzuide
 * Create at 2020/07/03 17:17
 */
public enum ExceptionEnum implements UniformExceptionDataAssert {

    PARAM_IS_NULL(4000, "用户:{0,number,#}, 参数不能为null.");

    private final int code;

    private final String message;

    ExceptionEnum(int code, String message) {
        this.code =  code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
