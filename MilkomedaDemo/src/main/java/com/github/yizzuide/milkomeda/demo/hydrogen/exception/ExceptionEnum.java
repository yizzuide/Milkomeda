package com.github.yizzuide.milkomeda.demo.hydrogen.exception;

import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformI18nExceptionDataAssert;

/**
 * ResponseEnum
 *
 * @author yizzuide
 * Create at 2020/07/03 17:17
 */
public enum ExceptionEnum implements UniformI18nExceptionDataAssert {
    // 如果不需要国际化，只需实现UniformExceptionDataAssert
    PARAM_IS_NULL(4000, "用户:{0,number,#}, 参数不能为null"), // 设置SubFormatPattern为#，让数值不显示千分符，如: 1,001
    // 需要国际化，需要实现UniformI18nExceptionDataAssert，MessageSource中的key通过ms.${}提取
    ID_IS_NULL(4001, "wow! ms.${uniform.code.4001}, ooh!");

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
