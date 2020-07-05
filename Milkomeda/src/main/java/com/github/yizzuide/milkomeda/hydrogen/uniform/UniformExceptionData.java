package com.github.yizzuide.milkomeda.hydrogen.uniform;

/**
 * UniformExceptionData
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/03 17:08
 */
public interface UniformExceptionData {
    /**
     * 异常code
     * @return  code
     */
    int getCode();

    /**
     * 异常消息
     * @return  String
     */
    String getMessage();
}
