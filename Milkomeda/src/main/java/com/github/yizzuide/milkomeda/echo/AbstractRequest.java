package com.github.yizzuide.milkomeda.echo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.universe.config.MilkomedaProperties;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AbstractRequest
 * 抽象请求类
 *
 * @author yizzuide
 * @since 1.13.0
 * @version 3.12.3
 * Create at 2019/09/21 16:48
 */
@Slf4j
public abstract class AbstractRequest {

    @Resource(name = "echoRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private MilkomedaProperties milkomedaProperties;

    /**
     * 获取消息体数据
     * 注意：消息体输入流只能读取一次，通过Filter包装一个Request才可以用这个方法
     *
     * @param inputStream InputStream
     * @return String
     */
    public static String getPostData(InputStream inputStream) {
        StringBuilder data;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String str;
            data = new StringBuilder();
            while((str = reader.readLine()) != null) {
                data.append(str);
            }
        } catch (IOException e) {
            log.error("abstractRequest:- get request body error with message: {}", e.getMessage(), e);
            return null;
        }
        return data.toString();
    }

    /**
     * Post方式向第三方发送请求，返回封装的ResponseData（默认data为Map类型）
     *
     * @param url 请求子路径
     * @param map 请求参数
     * @return EchoResponseData
     * @throws EchoException 请求异常
     */
    public EchoResponseData<Map<String, Object>> sendPostForResult(String url, Map<String, Object> map) throws EchoException {
        return sendPostForResult(url, map, new TypeReference<Map<String, Object>>() {});
    }

    /**
     * Post方式向第三方发送请求，返回封装的ResponseData（需要指定data的具体类型）
     *
     * @param url         请求子路径
     * @param params      请求参数
     * @param specType    具体类型
     * @param <T>         EchoResponseData的data字段类型
     * @return EchoResponseData
     * @throws EchoException 请求异常
     */
    public <T> EchoResponseData<T> sendPostForResult(String url, Map<String, Object> params, TypeReference<T> specType) throws EchoException {
        return sendPostForResult(url, params, specType, false);
    }

    /**
     * Post方式向第三方发送请求，返回封装的ResponseData（需要指定data的具体类型）
     *
     * @param url         请求子路径
     * @param params      请求参数
     * @param specType    EchoResponseData的data字段类型
     * @param forceCamel  是否强制驼峰字段，支持深度转换
     * @param <T>         EchoResponseData的data字段类型
     * @return EchoResponseData
     * @throws EchoException 请求异常
     */
    public <T> EchoResponseData<T> sendPostForResult(String url, Map<String, Object> params, TypeReference<T> specType, boolean forceCamel) throws EchoException {
        return sendRequest(HttpMethod.POST, url, null, params, specType, forceCamel);
    }

    /**
     * GET方式向第三方发送请求，返回封装的ResponseData（默认data为Map类型）
     * @param url   请求URL
     * @return  EchoResponseData
     * @throws EchoException 请求异常
     */
    public EchoResponseData<Map<String, Object>> sendGetForResult(String url) throws EchoException {
        return sendRequest(HttpMethod.GET, url, null, null, new TypeReference<Map<String, Object>>() {}, false);
    }

    /**
     * GET方式向第三方发送请求，返回封装的ResponseData（默认data为Map类型）
     * @param url       请求URL
     * @param params    请求参数
     * @return  EchoResponseData
     * @throws EchoException 请求异常
     */
    public EchoResponseData<Map<String, Object>> sendGetForResult(String url, Map<String, Object> params) throws EchoException {
        return sendRequest(HttpMethod.GET, url, null, params, new TypeReference<Map<String, Object>>() {}, false);
    }


    /**
     * 发送REST请求，返回封装的ResponseData（默认data为Map类型）
     * @param method    请求方式
     * @param url       请求URL
     * @param params    请求参数
     * @return  EchoResponseData
     * @throws EchoException 请求异常
     */
    public EchoResponseData<Map<String, Object>> sendRequestForResult(HttpMethod method, String url, Map<String, Object> params) throws EchoException {
        return sendRequest(method, url, null, params, new TypeReference<Map<String, Object>>() {}, false);
    }

    /**
     * 发送REST请求
     * @param method        请求方式
     * @param url           请求URL
     * @param headerMap       当前需要添加的请求头
     * @param params        请求参数，GET 和 DELETE 方式将会拼接在URL后面
     * @param specType      EchoResponseData的data字段类型
     * @param forceCamel    是否强制驼峰字段，支持深度转换
     * @param <T>           EchoResponseData的data字段类型
     * @return  EchoResponseData
     * @throws EchoException 请求异常
     */
    @SuppressWarnings("rawtypes")
    public <T> EchoResponseData<T> sendRequest(HttpMethod method, String url, Map<String, String> headerMap, Map<String, Object> params, TypeReference<T> specType, boolean forceCamel) throws EchoException {
        // 有消息体的请求方式
        boolean hasBody = hasBody(method);
        // 请求参数
        Map reqParams = new HashMap();
        // 请求头
        HttpHeaders headers = new HttpHeaders();
        boolean showLog = milkomedaProperties.isShowLog();
        // 执行请求
        ResponseEntity<String> request = performRequest(method, url, params, headerMap, reqParams, headers, hasBody, showLog, String.class);
        String body = request.getBody();
        boolean useStandardHTTP = useStandardHTTP();
        if (null == body) {
            if (!useStandardHTTP) {
                log.error("abstractRequest:- response with url: {}, params: {}, reqParams:{}, data: null", url, params, reqParams);
                throw new EchoException(ErrorCode.VENDOR_RESPONSE_IS_NOTHING, "response body is null");
            }
        }
        if (showLog) {
            log.info("abstractRequest:- response with url: {}, data: {}", url, body);
        }

        Object responseEntity = null;
        if (null != body) {
            boolean isMap = body.matches("^\\s*\\{.+");
            boolean isList = body.matches("^\\s*\\[.+");
            if (isMap) {
                responseEntity = JSONUtil.parseMap(body, String.class, Object.class);
            } else if (isList) {
                responseEntity = JSONUtil.parseList(body, Map.class);
            } else {
                throw new EchoException(ErrorCode.VENDOR_SERVER_RESPONSE_DATA_ANALYSIS_FAIL, "不支持的响应数据：" + body);
            }
            checkRawResponse(responseEntity);

            // 下划线转驼峰
            if (forceCamel && null != responseEntity) {
                try {
                    responseEntity = isMap ? JSONUtil.toCamel(responseEntity, new TypeReference<Map>() {}) :
                            JSONUtil.toCamel(responseEntity, new TypeReference<List>() {});
                } catch (Exception e) {
                    log.error("abstractRequest:- convert type data  error: {}", e.getMessage(), e);
                    throw new EchoException(ErrorCode.VENDOR_SERVER_RESPONSE_DATA_ANALYSIS_FAIL, e.getMessage());
                }
            }
        }

        EchoResponseData<T> responseData = createReturnData(responseEntity, specType, useStandardHTTP, forceCamel);
        if (useStandardHTTP) {
            responseData.setCode(String.valueOf(request.getStatusCodeValue()));
        }
        checkResponse(responseData);
        return responseData;
    }

    /**
     * 获取流数据
     * @param method    请求方式
     * @param url       请求URL
     * @param headerMap 源请求头
     * @param params    源请求参数
     * @return  InputStream
     */
    @SuppressWarnings("rawtypes")
    public InputStream sendRequest(HttpMethod method, String url, Map<String, String> headerMap, Map<String, Object> params) {
        // 有消息体的请求方式
        boolean hasBody = hasBody(method);
        // 请求参数
        Map reqParams = new HashMap();
        // 请求头
        HttpHeaders headers = new HttpHeaders();
        boolean showLog = milkomedaProperties.isShowLog();
        // 执行请求
        ResponseEntity<org.springframework.core.io.Resource> request = performRequest(method, url, params, headerMap, reqParams, headers, hasBody, showLog, org.springframework.core.io.Resource.class);
        if (null == request.getBody()) {
            log.error("abstractRequest:- response with url: {}, params: {}, reqParams:{}, data: null", url, params, reqParams);
            throw new EchoException(ErrorCode.VENDOR_RESPONSE_IS_NOTHING, "response body is null");
        }
        // 获取消息体
        org.springframework.core.io.Resource body = request.getBody();
        if (showLog) {
            log.info("abstractRequest:- response with url: {}, params: {}, reqParams:{}, data: {}", url, params, reqParams, body);
        }
        try {
            return body.getInputStream();
        } catch (IOException e) {
            throw new EchoException(ErrorCode.VENDOR_RESPONSE_IS_FAIL, e.getMessage());
        }
    }

    /**
     * 执行请求
     * @param method    请求方式
     * @param url       请求URL
     * @param params    源请求参数
     * @param headerMap 源请求头
     * @param reqParams 实际请求参数
     * @param hasBody   是否有消息体
     * @param headers   实际请求头
     * @param showLog   是否显示日志
     * @param respType  响应类型
     * @param <T>   响应类型
     * @return ResponseEntity
     */
    @SuppressWarnings("unchecked")
    private <T> ResponseEntity<T> performRequest(HttpMethod method, String url, Map<String, Object> params, Map<String, String> headerMap, Map reqParams, HttpHeaders headers, boolean hasBody, boolean showLog, Class<T> respType) {
        // 添加头数据
        appendHeaders(headers);
        if (null != headerMap) {
            for (String key : headerMap.keySet()) {
                headers.add(key, headerMap.get(key));
            }
        }

        if (hasBody) {
            // 表单类型，转LinkedMultiValueMap，支持value数组
            if (MediaType.APPLICATION_FORM_URLENCODED.equals(headers.getContentType()) ||
                    MediaType.MULTIPART_FORM_DATA.equals(headers.getContentType())) {
                reqParams = new LinkedMultiValueMap<String, Object>();
            }
        }

        // 设置参数默认值
        if (null == params) {
            params = new HashMap<>();
        }
        // 追加签名等参数
        signParam(params, reqParams);
        if (showLog) {
            log.info("abstractRequest:- send request with url: {}, params: {}, reqParams:{}", url, params, reqParams);
        }
        // 组装实体
        HttpEntity<Map> httpEntity = new HttpEntity<>(hasBody ? reqParams : null, headers);
        // 执行请求
        ResponseEntity<T> request;
        if (hasBody) {
            request = restTemplate.exchange(url, method, httpEntity, respType);
        } else {
            // 转换为URL参数
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
            for (Object k : reqParams.keySet()) {
                builder.queryParam((String) k, reqParams.get(k));
            }
            request = reqParams.size() > 0 ? restTemplate.exchange(builder.build().encode().toUri(), method, httpEntity, respType) :
                    restTemplate.exchange(url, method, httpEntity, respType);
        }
        return request;
    }

    private boolean hasBody(HttpMethod method) {
        return method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH;
    }

    /**
     * 是否使用标准的HTTP标准码（消息体中只有业务数据，不包code、msg这些）
     * @return 默认为false
     */
    protected boolean useStandardHTTP() {
        return false;
    }

    /**
     * 返回数据类型的模板方法
     *
     * @param <T>      EchoResponseData的data字段类型
     * @param respData 第三方方响应的数据，Map或List
     * @param specType ResponseData的data字段类型
     * @param useStandardHTTP 是否使用标准的HTTP标准码
     * @param forceCamel data里的字段是否强制下划线转驼峰
     * @return 统一响应数据类
     * @throws EchoException 请求异常
     */
    protected abstract <T> EchoResponseData<T> createReturnData(Object respData, TypeReference<T> specType, boolean useStandardHTTP, boolean forceCamel) throws EchoException;

    /**
     * 子类需要实现的参数签名（默认不应用签名）
     *
     * @param inParams  需要签名的业务参数
     * @param outParams 加上签名后的参数，如果Content-Type是APPLICATION_FORM_URLENCODED, 则类型为LinkedMultiValueMap，添加参数需要调用add方法
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void signParam(Map<String, Object> inParams, Map<String, Object> outParams) {
        if (outParams instanceof LinkedMultiValueMap) {
            LinkedMultiValueMap multiValueMap = (LinkedMultiValueMap) outParams;
            for (Map.Entry<String, Object> inEntry : inParams.entrySet()) {
                multiValueMap.add(inEntry.getKey(), inEntry.getValue());
            }
        } else {
            for (Map.Entry<String, Object> inEntry : inParams.entrySet()) {
                outParams.put(inEntry.getKey(), inEntry.getValue());
            }
        }
    }

    /**
     * 默认添加application/json，需要添加其它的头信息可以覆盖这个方法
     *
     * @param headers HttpHeaders
     */
    protected void appendHeaders(HttpHeaders headers) {
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
    }

    /**
     * 校验原始响应数据的正确性
     *
     * @param responseEntity 原始响应数据，Map或List
     * @throws EchoException 请求异常
     */
    protected void checkRawResponse(Object responseEntity) throws EchoException {}

    /**
     * 校验封装后的响应数据的正确性
     *
     * @param responseData 统一响应数据类
     * @throws EchoException 请求异常
     */
    @SuppressWarnings("rawtypes")
    protected void checkResponse(EchoResponseData responseData) throws EchoException {}

    /**
     * 对第三方平台的请求参数验签
     *
     * @param inParams  请求参数
     * @return  解签后的业务数据，解签失败返回null
     */
    public Map<String, Object> verifyParam(Map<String, Object> inParams) { return null; }
}
