package com.github.yizzuide.milkomeda.echo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.TypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;

/**
 * EchoRequest
 * 请求类实现，对接不同的第三方时需要继承这个类来创建自己的Request，可选覆盖实现自己的签名、验签、响应数据类适配、响应成功校验等方法
 *
 * @author yizzuide
 * @since 1.13.0
 * @since 1.13.6
 * Create at 2019/09/21 19:00
 */
@Slf4j
public abstract class EchoRequest extends AbstractRequest {

    @Override
    @SuppressWarnings("unchecked")
    protected <T> EchoResponseData<T> createReturnData(Object respData, TypeReference<T> specType, boolean useStandardHTTP) throws EchoException {
        if (null == respData) {
            return responseData();
        }
        // 指定的类型
        Class<?> specClazz = TypeUtil.type2Class(specType);
        boolean isMapType = Map.class == specClazz;
        boolean isListType = List.class == specClazz;

        // 指定类型判断
        if ((isListType && respData instanceof Map) || (isMapType && respData instanceof List)) {
            log.error("EchoRequest:- 响应类型匹配错误，当前响应类型为：{}，指定类型为：{}", respData.getClass(), specClazz);
            throw new EchoException(ErrorCode.ANALYSIS_DATA_FAIL, "响应类型匹配错误");
        }

        // 标准HTTP标准码处理方式
        if (useStandardHTTP) {
            // Map类型
            if (isMapType) {
                EchoResponseData<Map> responseData = responseData();
                responseData.setData((Map) respData);
                return (EchoResponseData<T>) responseData;
            }

            // List类型
            if (isListType) {
                EchoResponseData<List> responseData = responseData();
                responseData.setData((List) respData);
                return (EchoResponseData<T>) responseData;
            }

            // 指定的是其它自定义类型
            EchoResponseData<T> responseData = responseData();
            T body = JSONUtil.nativeRead(JSONUtil.serialize(respData), specType);
            responseData.setData(body);
            return responseData;
        }

        // 自定义通信协议响应处理
        EchoResponseData<T> responseData = responseData();
        try {
            // 处理不规范的数据（自定义通信协议一般顶层都是JSON Object)
            if (respData instanceof List) {
                responseData.setCode(String.valueOf(HttpStatus.OK.value()));
                responseData.setData((T) respData);
                log.warn("EchoRequest:- 当前响应的源数据不是JSON Object，而是JSON Array：{}", respData);
                return responseData;
            }
            BeanUtils.populate(responseData, (Map<String, Object>) respData);

            // 指定的是自定义的实体类型时，转换data到具体类型
            if (!(isMapType || isListType)) {
                responseData.setData(JSONUtil.nativeRead(JSONUtil.serialize(responseData.getData()), specType));
            }
        } catch (Exception e) {
            log.error("EchoRequest:- create return Data error: {}", e.getMessage(), e);
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
