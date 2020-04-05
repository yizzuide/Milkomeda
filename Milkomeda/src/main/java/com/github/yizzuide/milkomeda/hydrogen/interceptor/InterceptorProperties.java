package com.github.yizzuide.milkomeda.hydrogen.interceptor;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * InterceptorProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/06 01:12
 */
@Data
@ConfigurationProperties("milkomeda.hydrogen.interceptor")
public class InterceptorProperties {
    /**
     * 启用拦截器模块
     */
    private boolean enable = false;
    /**
     * 拦截器列表
     */
    private List<Interceptors> interceptors;

    @Data
    public static class Interceptors {
        /**
         * 拦截器类
         */
        private Class<?> clazz;

        /**
         * 包括拦截的URL
         */
        private List<String> includeURLs;

        /**
         * 排除拦截的URL
         */
        private List<String> excludeURLs;

        /**
         * 拦截器执行顺序
         */
        private int order = 0;

        /**
         * 属性
         */
        private Map<String, Object> props = new HashMap<>();

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Interceptors that = (Interceptors) o;
            return clazz.equals(that.clazz);
        }

        @Override
        public int hashCode() {
            return Objects.hash(clazz);
        }
    }
}
