package com.github.yizzuide.milkomeda.echo;

/**
 * ResponseData
 * 响应类规范接口，请求时返回类型统一使用这个接口
 * @param <T> data类型
 *
 * @author yizzuide
 * @since 1.13.0
 * @version 1.13.3
 * Create at 2019/09/21 17:13
 */
public interface EchoResponseData<T> {
    /**
     * 响应码
     * @return String
     */
    String getCode();
    default void setCode(String code) {};

    /**
     * 响应消息
     * @return String
     */
    String getMsg();
    default void setMsg(String msg) {};

    /**
     * 响应业务数据
     * @return T
     */
    T getData();
    default void setData(T respData) {};
}
