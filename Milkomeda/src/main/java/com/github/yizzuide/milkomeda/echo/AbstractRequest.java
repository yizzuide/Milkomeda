package com.github.yizzuide.milkomeda.echo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * AbstractRequest
 * 抽象请求类
 *
 * @author yizzuide
 * @since 1.13.1
 * Create at 2019/09/21 16:48
 */
@Slf4j
public abstract class AbstractRequest {

    @Resource(name = "echoRestTemplate")
    private RestTemplate restTemplate;

    /**
     * 获取消息体数据
     * 注意：消息体输入流只能读取一次，通过Filter包装一个Request才可以用这个方法
     *
     * @param request HttpServletRequest
     * @return String
     */
    public static String getPostData(HttpServletRequest request) {
        StringBuilder data;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
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
    @SuppressWarnings("unchecked")
    public <T> EchoResponseData<T> sendPostForResult(String url, Map<String, Object> params, TypeReference<T> specType, boolean forceCamel) throws EchoException {
        // 请求头
        HttpHeaders headers = new HttpHeaders();
        appendHeaders(headers);
        // 请求参数
        Map reqParams;
        // 表单类型
        if (MediaType.APPLICATION_FORM_URLENCODED.equals(headers.getContentType()) ||
                MediaType.MULTIPART_FORM_DATA.equals(headers.getContentType())) {
            reqParams = new LinkedMultiValueMap<String, Object>();
        } else { // JSON类型
            reqParams = new HashMap();
        }
        // 追加签名参数
        signParam(params, reqParams);
        HttpEntity<Map> httpEntity = new HttpEntity<>(reqParams, headers);

        log.info("abstractRequest:- send request with url: {}, params: {}, reqParams:{}", url, params, reqParams);
        ResponseEntity<Map> request = restTemplate.postForEntity(url, httpEntity, Map.class);
        Map body = request.getBody();
        if (null == body) {
            log.error("abstractRequest:- response with url: {}, params: {}, reqParams:{}, data: null", url, params, reqParams);
            throw new EchoException("The load platform response data is null");
        }
        log.info("abstractRequest:- response with url: {}, params: {}, reqParams:{}, data: {}", url, params, reqParams, body);
        // 下划线转驼峰
        if (forceCamel) {
            try {
                body = JSONUtil.toCamel(body, new TypeReference<Map>() {});
            } catch (Exception e) {
                log.error("abstractRequest:- convert type data  error: {}", e.getMessage(), e);
                throw new EchoException(ErrorCode.VENDOR_SERVER_RESPONSE_DATA_ANALYSIS_FAIL, e.getMessage());
            }
        }
        EchoResponseData<T> responseData = createReturnData(body, specType);
        checkResponse(responseData);
        return responseData;
    }

    /**
     * 返回数据类型的模板方法
     *
     * @param respData 第三方方响应的数据
     * @param specType ResponseData的data字段类型
     * @param <T>      EchoResponseData的data字段类型
     * @return 统一响应数据类
     * @throws EchoException 请求异常
     */
    protected abstract <T> EchoResponseData<T> createReturnData(Map respData, TypeReference<T> specType) throws EchoException;

    /**
     * 子类需要实现的参数签名（默认不应用签名）
     *
     * @param inParams  需要签名的业务参数
     * @param outParams 加上签名后的参数，如果Content-Type是APPLICATION_FORM_URLENCODED, 则类型为LinkedMultiValueMap，添加参数需要调用add方法
     */
    @SuppressWarnings("unchecked")
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
     * 检测响应数据的正确性
     *
     * @param responseData 统一响应数据类
     * @throws EchoException 请求异常
     */
    protected void checkResponse(EchoResponseData responseData) throws EchoException {}

    /**
     * 对第三方平台的请求参数验签
     *
     * @param inParams  请求参数
     * @return  解签后的业务数据，解签失败返回null
     */
    public Map<String, Object> verifyParam(Map<String, Object> inParams) { return null; }
}
