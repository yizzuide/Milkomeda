package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.NetworkUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * CometData
 *
 * @author yizzuide
 * @since 1.12.0
 * @version 3.0.0
 * Create at 2019/04/11 19:32
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"requestHeaders", "deviceInfo"})
public class WebCometData extends CometData {
    private static final long serialVersionUID = -2078666744044889106L;
    /**
     * 接口请求序号
     */
    private String apiCode;
    /**
     * 请求类型 1: 前台请求（默认） 2：第三方服务器推送
     */
    private String requestType;
    /**
     * 请求 URL
     */
    private String requestURL;
    /**
     * 请求路径
     */
    private String requestPath;
    /**
     * 请求方式
     */
    private String requestMethod;
    /**
     * 请求参数（使用SpringMVC开发的接口同时支持form表单数据和自定义消息体数据）
     */
    private String requestParams;
    /**
     * 请求头
     */
    private String requestHeaders;
    /**
     * 请求 IP
     */
    private String requestIP;
    /**
     * 请求设备信息
     */
    private String deviceInfo;

    /**
     * 根据请求创建
     * @param request           HttpServletRequest
     * @param prototype         实例原型
     * @param readRequestBody   是否允许读取body
     * @return  WebCometData
     */
    public static WebCometData createFormRequest(HttpServletRequest request, Class<? extends WebCometData> prototype,  boolean readRequestBody) {
        WebCometData cometData;
        try {
            cometData = prototype == null ? new WebCometData() : prototype.newInstance();
        } catch (Exception e) {
            log.info("Comet create WebCometData error with msg: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Comet create WebCometData from prototype: " + prototype + " error");
        }
        cometData.setRequestURL(request.getRequestURL().toString());
        cometData.setRequestPath(request.getRequestURI());
        cometData.setRequestMethod(request.getMethod());
        String requestParams = CometAspect.resolveThreadLocal.get();
        cometData.setRequestParams(requestParams != null ? requestParams :
                CometAspect.resolveRequestParams(request, readRequestBody));
        Map<String, Object> headers = new HashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            headers.put(key, request.getHeader(key));
        }
        cometData.setRequestHeaders(JSONUtil.serialize(headers));
        cometData.setRequestIP(NetworkUtil.getRemoteAddr(request));
        cometData.setDeviceInfo(request.getHeader("user-agent"));
        return cometData;
    }
}
