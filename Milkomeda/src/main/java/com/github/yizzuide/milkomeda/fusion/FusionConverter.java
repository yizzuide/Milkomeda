package com.github.yizzuide.milkomeda.fusion;

/**
 * FusionConverter
 *
 * @author yizzuide
 * @since 2.2.0
 * @version 3.0.0
 * Create at 2020/01/04 11:14
 */
@FunctionalInterface
public interface FusionConverter<T, O, R> {
    /**
     * 错误消息前缀
     */
    String ERROR_PREFIX = "#E.";

    /**
     * 构建错误消息
     * @param msg   原消息
     * @return  错误消息
     */
    static String buildError(String msg) {
        return ERROR_PREFIX + msg;
    }

    /**
     * 修改返回值
     * @param tag           转换类型tag
     * @param returnObj     返回的原方法数据
     * @param error         业务错误
     * @return  替换后的返回数据
     */
    R apply(T tag, O returnObj, String error);
}
