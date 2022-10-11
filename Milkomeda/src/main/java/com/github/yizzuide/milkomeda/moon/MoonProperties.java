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

package com.github.yizzuide.milkomeda.moon;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Map;

/**
 * MoonProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.7.0
 * <br>
 * Create at 2020/03/28 17:24
 */
@Data
@ConfigurationProperties("milkomeda.moon")
public class MoonProperties {
    /**
     * 是否使用Light+Atom混和方案，否则使用高性能lua脚本方式（如无扩展环形策略需求，保持默认即可）
     * @since 3.7.0
     */
    private boolean mixinMode = false;

    /**
     * 实例配置
     */
    private List<Instance> instances;

    @Data
    public static class Instance {
        /**
         * 实例名（不能与Spring IoC里的bean有重名)
         */
        private String name;

        /**
         * Light L2缓存实例名（{@link MoonProperties#mixinMode} 为true时有效）
         */
        private String cacheName = Moon.CACHE_NAME;

        /**
         * 环形策略类型
         */
        private MoonType type = MoonType.PERIODIC;

        /**
         * 自定义环形策略（如果这个有值，则忽略type）
         */
        private Class<? extends MoonStrategy> moonStrategyClazz;

        /**
         * 自定义属性
         */
        private Map<String, Object> props;

        /**
         * 环形阶段值
         */
        private List<Object> phases;
    }
}
