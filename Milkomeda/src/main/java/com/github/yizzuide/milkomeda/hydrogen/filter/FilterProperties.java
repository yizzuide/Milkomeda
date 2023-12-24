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
 * <br>
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
        private Class<? extends jakarta.servlet.Filter> clazz;

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
