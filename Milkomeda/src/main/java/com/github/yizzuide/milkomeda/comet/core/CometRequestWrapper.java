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

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.HttpServletUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.OrderComparator;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.net.SocketTimeoutException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CometRequestWrapper
 * 请求包装，用于重复读取请求消息体内容
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.15.0
 * @see org.springframework.web.util.ContentCachingRequestWrapper
 * <br>
 * Create at 2019/12/12 17:37
 */
@Slf4j
public class CometRequestWrapper extends HttpServletRequestWrapper {
    /**
     * Cache request body data.
     */
    private byte[] body;

    private final HttpServletRequest originalRequest;

    // 请求拦截器
    private static List<CometRequestInterceptor> INTERCEPTOR_LIST;

    /**
     * Create Request wrapper for intercept or cache body.
     * @param request   HttpServletRequest
     * @param cacheBody enable cache body
     */
    public CometRequestWrapper(HttpServletRequest request, boolean cacheBody) {
        super(request);
        originalRequest = request;
        // 将body数据存储起来
        if (cacheBody) {
            String bodyStr = getBodyString(request);
            if (StringUtils.isEmpty(bodyStr)) {
                body = new byte[0];
                return;
            }
            body = bodyStr.getBytes(Charset.defaultCharset());
        }

        // 加载请求拦截器
        if (INTERCEPTOR_LIST == null) {
            INTERCEPTOR_LIST = new ArrayList<>();
            ObjectProvider<CometRequestInterceptor> beanProvider = ApplicationContextHolder.get().getBeanProvider(CometRequestInterceptor.class);
            beanProvider.forEach(INTERCEPTOR_LIST::add);
            if (!CollectionUtils.isEmpty(INTERCEPTOR_LIST)) {
                INTERCEPTOR_LIST = INTERCEPTOR_LIST.stream()
                        .sorted(OrderComparator.INSTANCE.withSourceProvider(itr -> itr)).collect(Collectors.toList());
            }
        }
    }

    @Override
    public String getParameter(String name) {
        String value = super.getParameter(name);
        if (CollectionUtils.isEmpty(INTERCEPTOR_LIST)) {
            return value;
        }
        return interceptRequest(name, value, null);
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values == null || CollectionUtils.isEmpty(INTERCEPTOR_LIST)) {
            return values;
        }
        for (int i=0; i < values.length; i++) {
            values[i] = interceptRequest(name, values[i], null);
        }
        return values;
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        Map<String, String[]> parameterMap = super.getParameterMap();
        if (CollectionUtils.isEmpty(parameterMap) || CollectionUtils.isEmpty(INTERCEPTOR_LIST)) {
            return parameterMap;
        }
        Map<String, String[]> modifyParamMap = new HashMap<>(parameterMap);
        modifyParamMap.keySet().forEach(key -> modifyParamMap.replace(key, getParameterValues(key)));
        return modifyParamMap;
    }

    // 在返回值前拦截
    private String interceptRequest(String name, String value, String body) {
        for (CometRequestInterceptor interceptor: INTERCEPTOR_LIST) {
            value = interceptor.readFromRequest(originalRequest, name, value, body);
        }
        return value;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream()));
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        final ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
        return new ServletInputStream() {
            @Override
            public int read() throws IOException {
                if (body.length == 0) {
                    return -1;
                }
                return inputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
            }
        };
    }

    /**
     * 获取请求参数
     * @param request   HttpServletRequest
     * @param formBody  是否从消息体获取
     * @return  JSON格式化字符串
     * @since 3.0.3
     */
    public static String resolveRequestParams(HttpServletRequest request, boolean formBody) {
        CometRequestWrapper requestWrapper = WebUtils.getNativeRequest(request, CometRequestWrapper.class);
        if (requestWrapper == null) {
            request = new CometRequestWrapper(request, false);
        }
        String requestData = HttpServletUtil.getRequestData(request);
        // 如果form方式获取为空，取消息体内容
        if (formBody && "{}".equals(requestData)) {
            if (requestWrapper == null) {
                return requestData;
            }
            // 从请求包装里获取
            String body = requestWrapper.getBodyString();
            // 删除换行符
            body = body == null ? "" : body.replaceAll("\\n?\\t?", "");
            return body;
        }
        return requestData;
    }

    /**
     * 获取请求Body
     *
     * @return String
     */
    public String getBodyString() {
        final InputStream inputStream = new ByteArrayInputStream(body);
        return inputStream2String(inputStream);
    }

    /**
     * 获取请求Body
     *
     * @param request request
     * @return String
     */
    private String getBodyString(final ServletRequest request) {
        try {
            return inputStream2String(request.getInputStream());
        } catch (IOException e) {
            log.error("Comet get input stream error:{}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 将流里的数据读取出来并转换成字符串
     *
     * @param inputStream inputStream
     * @return String
     */
    private String inputStream2String(InputStream inputStream) {
        // 如果消息体没有数据
        if (inputStream == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, Charset.defaultCharset()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        } catch (ClientAbortException | SocketTimeoutException ignore) {
            return null;
        } catch (IOException e) {
            log.error("Comet read input stream error with msg: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
        String body = sb.toString();
        if (CollectionUtils.isEmpty(INTERCEPTOR_LIST)) {
            return body;
        }
        return interceptRequest(null, null, body);
    }
}
