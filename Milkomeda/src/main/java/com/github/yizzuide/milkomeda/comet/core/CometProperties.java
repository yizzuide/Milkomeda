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

import com.github.yizzuide.milkomeda.universe.extend.web.handler.HotHttpHandlerProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * CometProperties
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.20.0
 * <br>
 * Create at 2019/12/12 18:04
 */
@Data
@ConfigurationProperties(CometProperties.PREFIX)
public class CometProperties {

    public static final String PREFIX = "milkomeda.comet";

    /**
     * 允许开启请求包装类读取请求消息体（收集application/json类型消息体请求日志，或使用 {@link CometParam } 时必须开启）
     */
    private boolean enableReadRequestBody = false;

    /**
     * 允许开启响应包装类读取响应消息体（获取通过注入HttpServletResponse直接写出响应数据则必须开启）
     *
     * @see CometProperties#enableReadRequestBody
     */
    private boolean enableReadResponseBody = false;

    /**
     * 请求与响应包装类需要排除的URL
     * @since 3.20.0
     */
    private List<String> excludeUrls = Arrays.asList("/favicon.ico", "/druid/**", "/doc.html", "/webjars/**",
            "/swagger-resources/**", "/swagger-ui.html");

    /**
     * 成功状态码
     */
    private int statusSuccessCode = 0;

    /**
     * 失败状态码
     */
    private int statusFailCode = 1;

    /**
     * Config request parameter interceptor.
     * @since 3.15.0
     */
    private Map<String, HotHttpHandlerProperty> requestInterceptors;

    /**
     * Config response interceptor.
     * @since 3.15.0
     */
    private Map<String, HotHttpHandlerProperty> responseInterceptors;
}
