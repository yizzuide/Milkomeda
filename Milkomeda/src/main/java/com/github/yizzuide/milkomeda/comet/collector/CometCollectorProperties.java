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

package com.github.yizzuide.milkomeda.comet.collector;

import com.github.yizzuide.milkomeda.comet.core.CometProperties;
import com.github.yizzuide.milkomeda.comet.core.WebCometData;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CometCollectorProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * <br />
 * Create at 2020/03/28 21:28
 */
@Data
@ConfigurationProperties("milkomeda.comet.collector")
public class CometCollectorProperties {

    /**
     * 启用日志收集器
     */
    private boolean enable = false;

    /**
     * 开启URL标签日志收集，通过注入HttpServletResponse直接写出流需要开启 {@link CometProperties#isEnableReadResponseBody()}
     */
    private boolean enableTag = false;

    /**
     * 标签集合
     */
    private Map<String, Tag> tags = new HashMap<>();

    @Data
    public static class Tag {
        /**
         * CometData原型类
         */
        private Class<? extends WebCometData> prototype = WebCometData.class;

        /**
         * 匹配包含的路径
         */
        private List<String> include;

        /**
         * 排除路径
         */
        private List<String> exclude;


        /**
         * 异常监控器（由于异常可能被 @ControllerAdvice 吞没）
         */
        private Map<String, Object> exceptionMonitor;
    }
}
