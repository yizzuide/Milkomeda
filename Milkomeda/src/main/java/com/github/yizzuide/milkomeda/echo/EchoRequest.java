package com.github.yizzuide.milkomeda.echo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.TypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * EchoRequest
 * 请求类实现，对接不同的第三方时需要继承这个类来创建自己的Request，可选覆盖实现自己的签名、验签、响应数据类适配、响应成功校验等方法
 *
 * @author yizzuide
 * @since 1.13.0
 * @version 3.12.3
 * Create at 2019/09/21 19:00
 */
@Slf4j
public abstract class EchoRequest extends AbstractRequest {

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected <T> EchoResponseData<T> createReturnData(Object respData, TypeReference<T> specType, boolean useStandardHTTP, boolean forceCamel) throws EchoException {
        if (null == respData) {
            return responseData();
        }
        // 指定的类型
        Class<?> specClazz = TypeUtil.type2Class(specType);
        boolean isStringType = String.class == specClazz;
        boolean isMapType = Map.class == specClazz;
        boolean isListType = List.class == specClazz;

        // 标准HTTP标准码处理方式
        if (useStandardHTTP) {
            // 指定类型判断
            if ((isListType && respData instanceof Map) || (isMapType && respData instanceof List)) {
                log.error("EchoRequest:- 响应类型匹配错误，当前响应类型为：{}，指定类型为：{}", respData.getClass(), specClazz);
                throw new EchoException(ErrorCode.ANALYSIS_DATA_FAIL, "响应类型匹配错误");
            }
            // Map类型
            if (isMapType) {
                EchoResponseData<Map> responseData = responseData();
                responseData.setData((Map) respData);
                return (EchoResponseData<T>) responseData;
            }

            // List类型
            if (respData instanceof List && isListType) {
                // 如果没有参数子类型
                if (specType.getType() instanceof Class) {
                    EchoResponseData<List> responseData = responseData();
                    responseData.setData((List) respData);
                    return (EchoResponseData<T>) responseData;
                }

                // 有参数类型
                ParameterizedType parameterizedType = (ParameterizedType) specType.getType();
                Type paramType = parameterizedType.getActualTypeArguments()[0];
                if (TypeUtil.type2Class(paramType) == Map.class) {
                    EchoResponseData<List> responseData = responseData();
                    responseData.setData((List) respData);
                    return (EchoResponseData<T>) responseData;
                }
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

            // 有规范格式的响应数据
            if (respData instanceof Map) {
                BeanUtils.populate(responseData, (Map) respData);
            }

            // 如果消息体为空，直接返回
            if (responseData.getData() == null) {
                return responseData;
            }

            // data内容识别转换
            if (responseData.getData() instanceof String) {
                // 指定的为字符串类型直接返回
                if (isStringType) {
                    return responseData;
                }
                if (forceCamel) {
                    responseData.setData(JSONUtil.toCamel(responseData.getData(), specType));
                    return responseData;
                }
                responseData.setData(JSONUtil.nativeRead((String) responseData.getData(), specType));
                return responseData;
            }

            // 指定类型为非Map的处理
            if (!isMapType) {
                // 如果指定是List
                if (isListType) {
                    if (!(responseData.getData() instanceof List)) {
                        log.error("EchoRequest:- 响应类型匹配错误，当前响应类型为：{}，指定类型为：{}", responseData.getData().getClass(), specClazz);
                        throw new EchoException(ErrorCode.ANALYSIS_DATA_FAIL, "响应类型匹配错误");
                    }
                    // 没有参数类型，不处理
                    if (specType.getType() instanceof Class) {
                        return responseData;
                    }
                    // 参数类型是Map，不处理
                    ParameterizedType parameterizedType = (ParameterizedType) specType.getType();
                    Type paramType = parameterizedType.getActualTypeArguments()[0];
                    if (TypeUtil.type2Class(paramType) == Map.class) {
                        return responseData;
                    }
                }
                if (responseData.getData() instanceof String) {
                    responseData.setData(JSONUtil.nativeRead((String) responseData.getData(), specType));
                } else {
                    responseData.setData(JSONUtil.nativeRead(JSONUtil.serialize(responseData.getData()), specType));
                }
            }
        } catch (Exception e) {
            log.error("EchoRequest:- create return Data error: {}", e.getMessage(), e);
            throw new EchoException(e.getMessage());
        }
        return responseData;
    }

    /**
     * 获取数据
     * @param method    请求方式
     * @param url       请求URL
     * @return  EchoResponseData
     */
    public EchoResponseData<Map<String, Object>> fetch(HttpMethod method, String url) {
        return fetch(method, url, null);
    }

    /**
     * 获取数据列表
     * @param method    请求方式
     * @param url       请求URL
     * @return  EchoResponseData
     */
    public EchoResponseData<List<Map<String, Object>>> fetchList(HttpMethod method, String url) {
        return fetchList(method, url, null);
    }

    /**
     * 获取数据
     * @param method    请求方式
     * @param url       请求URL
     * @param params    请求参数
     * @return  EchoResponseData
     */
    public EchoResponseData<Map<String, Object>> fetch(HttpMethod method, String url, Map<String, Object> params) {
        return fetch(method, url, null, params);
    }

    /**
     * 获取数据列表
     * @param method    请求方式
     * @param url       请求URL
     * @param params    请求参数
     * @return  EchoResponseData
     */
    public EchoResponseData<List<Map<String, Object>>> fetchList(HttpMethod method, String url, Map<String, Object> params) {
        return fetchList(method, url, null, params);
    }

    /**
     * 获取数据
     * @param method    请求方式
     * @param url       请求URL
     * @param headerMap 请求头
     * @param params    请求参数
     * @return  EchoResponseData
     */
    public EchoResponseData<Map<String, Object>> fetch(HttpMethod method, String url, Map<String, String> headerMap, Map<String, Object> params) {
        return fetch(method, url, headerMap, params, false);
    }

    /**
     * 获取数据列表
     * @param method    请求方式
     * @param url       请求URL
     * @param headerMap 请求头
     * @param params    请求参数
     * @return  EchoResponseData
     */
    public EchoResponseData<List<Map<String, Object>>> fetchList(HttpMethod method, String url, Map<String, String> headerMap, Map<String, Object> params) {
        return fetchList(method, url, headerMap, params, false);
    }

    /**
     * 获取数据
     * @param method        请求方式
     * @param url           请求URL
     * @param headerMap     请求头
     * @param params        请求参数
     * @param forceCamel    响应字段转为Camel风格
     * @return  EchoResponseData
     */
    public EchoResponseData<Map<String, Object>> fetch(HttpMethod method, String url, Map<String, String> headerMap, Map<String, Object> params, boolean forceCamel) {
        return sendRequest(method, url, headerMap, params, new TypeReference<Map<String, Object>>() {}, forceCamel);
    }

    /**
     * 获取数据列表
     * @param method        请求方式
     * @param url           请求URL
     * @param headerMap     请求头
     * @param params        请求参数
     * @param forceCamel    响应字段转为Camel风格
     * @return  EchoResponseData
     */
    public EchoResponseData<List<Map<String, Object>>> fetchList(HttpMethod method, String url, Map<String, String> headerMap, Map<String, Object> params, boolean forceCamel) {
        return sendRequest(method, url, headerMap, params, new TypeReference<List<Map<String, Object>>>() {}, forceCamel);
    }

    /**
     * EchoRequest子类需要实现并返回自己的EchoResponseData类型实例
     * @param <T>   data字段类型
     * @return  EchoResponseData子类型
     */
    protected abstract <T> EchoResponseData<T> responseData();
}
