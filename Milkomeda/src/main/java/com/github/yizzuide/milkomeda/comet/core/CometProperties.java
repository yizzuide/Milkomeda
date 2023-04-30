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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * CometProperties
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.5.0
 * <br>
 * Create at 2019/12/12 18:04
 */
@Data
@ConfigurationProperties("milkomeda.comet")
public class CometProperties {

    /**
     * 允许开启请求包装类读取请求消息体（收集application/json类型消息体请求日志，或使用 {@link CometParam } 时必须开启）
     */
    private boolean enableReadRequestBody = false;

    /**
     * 允许开启响应包装类读取响应消息体（获取通过注入HttpServletResponse直接写出响应数据则必须开启）
     * @see CometProperties#enableReadRequestBody
     */
    private boolean enableReadResponseBody = false;

    /**
     * 成功状态码
     */
    private String statusSuccessCode = "1";

    /**
     * 失败状态码
     */
    private String statusFailCode = "2";

    /**
     * Web XSS protection.
     * @since 3.15.0
     */
    private Xss xss = new Xss();

    @Data
    static class Xss {
        /**
         * Enable XSS prevent.
         */
        private boolean prevent = false;

        /**
         * White filed names is not prevent (only support form submit type).
         */
        private List<String> whiteFieldNames;

        /**
         * URL list need prevent.
         */
        private List<String> includeUrls = Collections.singletonList("/**");

        /**
         * URL list not prevent.
         */
        private List<String> excludeUrls;
    }
}
