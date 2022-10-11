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

package com.github.yizzuide.milkomeda.comet.logger;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * CometLoggerProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.11.0
 * <br>
 * Create at 2020/04/05 18:47
 */
@Data
@ConfigurationProperties("milkomeda.comet.logger")
public class CometLoggerProperties {
    /**
     * 启用请求日志打印
     */
    private boolean enable = false;
    /**
     * 是否开启打印响应
     */
    private boolean enableResponse = false;
    /**
     * 占位符前缀
     */
    private String prefix = "{";
    /**
     * 占位符后缀
     */
    private String suffix = "}";
    /**
     * 需要排除打印的路径
     */
    private List<String> exclude;
    /**
     * 打印策略
     */
    private List<Strategy> strategy = Collections.singletonList(new Strategy());

    @Data
    public static class Strategy {
        /**
         * 打印日志类型
         * @since 3.11.0
         */
        private CometLoggerType type = CometLoggerType.REQUEST;
        /**
         * 策略包含路径
         */
        private List<String> paths = Collections.singletonList("/**");
        /**
         * 策略模板（固定占位符：uri、method、params；请求参数域/自定义解析参数：$params.name；请求头域：$header.name；cookie域：$cookie.name）
         */
        private String tpl = "{\"uri\":\"{uri}\", \"method\": \"{method}\", \"params\": \"{params}\", \"token\": \"{$header.token}\"}";
        /**
         * 缓存占位符（模块内部使用）
         */
        private Map<String, List<String>> cacheKeys;
    }
}
