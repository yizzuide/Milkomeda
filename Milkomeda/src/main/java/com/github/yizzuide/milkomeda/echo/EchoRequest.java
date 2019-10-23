package com.github.yizzuide.milkomeda.echo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.TypeUtil;
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
    protected <T> EchoResponseData<T> createReturnData(Map respData, TypeReference<T> specType, boolean useStandardHTTP) throws EchoException {
        if (null == respData) {
            return responseData();
        }
        boolean isMapType = Map.class == TypeUtil.type2Class(specType);
        // 标准HTTP标准码处理方式
        if (useStandardHTTP) {
            // 如果指定的是Map类型
            if (isMapType) {
                EchoResponseData<Map> responseData = responseData();
                responseData.setData(respData);
                return (EchoResponseData<T>) responseData;
            }

            // 指定的是自定义类型
            EchoResponseData<T> responseData = responseData();
            T body = JSONUtil.nativeRead(JSONUtil.serialize(respData), specType);
            responseData.setData(body);
            return responseData;
        }

        // 自定义通信协议响应处理
        EchoResponseData<T> responseData = responseData();
        try {
            BeanUtils.populate(responseData, respData);
            // 指定的是自定义类型
            if (!isMapType) {
                responseData.setData(JSONUtil.nativeRead(JSONUtil.serialize(responseData.getData()), specType));
            }
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
