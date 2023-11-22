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

package com.github.yizzuide.milkomeda.particle;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.core.Ordered;
import org.springframework.http.MediaType;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * ParticleProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.5.0
 * <br>
 * Create at 2020/04/08 11:12
 */
@Data
@ConfigurationProperties("milkomeda.particle")
public class ParticleProperties {
    /**
     * 开启请求过滤（使用配置URL添加拦截时必须开启）
     */
    private boolean enableFilter = false;

    /**
     * 全局排除的URL
     */
    private List<String> excludeUrls;

    /**
     * 全局包含的URL
     */
    private List<String> includeUrls;

    /**
     * 全局限制后的响应
     */
    private Map<String, Object> response;

    /**
     * 限制处理器列表
     */
    private List<Limiter> limiters;


    @Data
    public static class Limiter implements Ordered {

        // 响应数据格式类型
        static final String RESPONSE_CONTENT_TYPE = "response_content_type";

        // 文本类型响应字段，用于在response里添加
        static final String RESPONSE_CONTENT = "content";

        /**
         * 限制处理器自动注册的Bean名（用于注解方式或lazy注入，Bean名不可重复）
         */
        private String name;

        /**
         * 排序
         */
        private int order = 0;

        /**
         * 限制器类型
         */
        private LimiterType type = LimiterType.IDEMPOTENT;

        /**
         * 自定义限制处理器类（如果这个有值，则忽略type）
         */
        private Class<? extends LimitHandler> handlerClazz;

        /**
         * 限制处理器属性
         */
        private Map<String, Object> props;

        /**
         * 分布式key模板：
         * <pre>
         *  固定占位符：uri、method、params
         *  请求参数域/自定义解析参数：$params.name
         *  请求头域：$header.name
         *  cookie域：$cookie.name
         * </pre>
         */
        private String keyTpl = "limit_{method}_{uri}_{$header.token}";

        /**
         * 分布式key过期时间
         */
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration keyExpire = Duration.ofSeconds(60);

        /**
         * 排除的URL
         */
        private List<String> excludeUrls;

        /**
         * 包含的URL
         */
        private List<String> includeUrls = Collections.singletonList("/**");

        /**
         * 响应类型
         * @since 3.5.0
         */
        private String responseContentType = MediaType.APPLICATION_JSON_VALUE;

        /**
         * 限制后的响应
         */
        private Map<String, Object> response;

        /**
         * 限制器实例（内部使用）
         */
        private LimitHandler limitHandler;

        /**
         * 缓存占位符（内部使用）
         */
        private Map<String, List<String>> cacheKeys;

        void setLimitHandler(LimitHandler limitHandler) {
            this.limitHandler = limitHandler;
        }

        LimitHandler getLimitHandler() {
            return  this.limitHandler;
        }

        void setCacheKeys(Map<String, List<String>> cacheKeys) {
            this.cacheKeys = cacheKeys;
        }

        Map<String, List<String>> getCacheKeys() {
            return this.cacheKeys;
        }

    }
}
