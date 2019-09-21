package com.github.yizzuide.milkomeda.echo;

/**
 * ResponseData
 * 响应类规范接口，请求时返回类型统一使用这个接口
 *
 * @author yizzuide
 * @since 1.13.0
 * Create at 2019/09/21 17:13
 */
public interface EchoResponseData<T> {
    /** 响应码 */
    String getCode();

    /** 响应消息 */
    String getMsg();

    /** 响应业务数据 */
    T getData();
}
