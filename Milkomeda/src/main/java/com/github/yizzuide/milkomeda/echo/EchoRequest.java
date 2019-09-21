package com.github.yizzuide.milkomeda.echo;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;

import java.util.Map;

/**
 * EchoRequest
 * 请求类实现，对接不同的第三方时需要继承这个类来创建自己的Request，可选覆盖实现自己的签名、验签、响应数据类适配、响应成功校验等方法
 *
 * @author yizzuide
 * @since 1.13.0
 * Create at 2019/09/21 19:00
 */
@Slf4j
public abstract class EchoRequest extends AbstractRequest {

    @Override
    @SuppressWarnings("unchecked")
    protected <T> EchoResponseData<T> createReturnData(Map respData, TypeReference<T> specType) throws EchoException {
        EchoResponseData<T> responseData = responseData();
        try {
            BeanUtils.populate(responseData, respData);
        } catch (Exception e) {
            log.error("EchoRequest create return Data error: {}", e.getMessage(), e);
            throw new EchoException(e.getMessage());
        }
        return responseData;
    }

    /**
     * EchoRequest子类需要实现并返回自己的EchoResponseData类型实例
     * @param <T>   data字段类型
     * @return  EchoResponseData子类型
     */
    protected abstract <T> EchoResponseData<T> responseData();
}
