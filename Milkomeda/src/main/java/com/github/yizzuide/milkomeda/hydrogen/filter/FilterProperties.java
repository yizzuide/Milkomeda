package com.github.yizzuide.milkomeda.hydrogen.filter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * FilterProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/06 01:22
 */
@Data
@ConfigurationProperties("milkomeda.hydrogen.filter")
public class FilterProperties {
    /**
     * 启用过滤模块
     */
    private boolean enable = false;

    /**
     * 过滤器列表
     */
    private List<Filters> filters;

    @Data
    public static class Filters {
        /**
         * 过滤器名
         */
        private String name;
        /**
         * 过滤器类
         */
        private Class<? extends javax.servlet.Filter> clazz;

        /**
         * 匹配的URL
         */
        private List<String> urlPatterns = Collections.singletonList("/*");

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Filters filters = (Filters) o;
            return name.equals(filters.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name);
        }
    }
}
