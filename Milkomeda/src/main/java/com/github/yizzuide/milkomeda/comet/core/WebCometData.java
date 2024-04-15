/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.NetworkUtil;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
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
 * @version 3.20.0
 * <br>
 * Create at 2019/04/11 19:32
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = {"requestHeaders", "deviceInfo"})
public class WebCometData extends CometData {
    
    private static final long serialVersionUID = -2078666744044889106L;

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
     * 请求客户端信息（请求头：user-agent）
     */
    private String deviceInfo;

    /**
     * 服务器地址
     */
    private String host;

    /**
     * 根据请求创建
     * @param request           HttpServletRequest
     * @param prototype         实例原型
     * @param readRequestBody   是否允许读取body
     * @return  WebCometData
     */
    public static WebCometData createFormRequest(HttpServletRequest request, Class<? extends WebCometData> prototype,  boolean readRequestBody) {
        WebCometData cometData = prototype == null ? new WebCometData() : ReflectUtil.newNonNullInstance(prototype);
        cometData.setRequestURL(request.getRequestURL().toString());
        cometData.setRequestPath(request.getRequestURI());
        cometData.setRequestMethod(request.getMethod());
        String requestParams = CometAspect.resolveThreadLocal.get();
        cometData.setRequestParams(requestParams != null ? requestParams :
                CometRequestWrapper.resolveRequestParams(request, readRequestBody));
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
