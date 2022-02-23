/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.orbit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OrbitProperties
 *
 * @author yizzuide
 * @since 3.13.0
 * Create at 2022/02/21 01:27
 */
@Data
@ConfigurationProperties(prefix = OrbitProperties.PREFIX)
public class OrbitProperties {
    // 当前配置前缀
    public static final String PREFIX = "milkomeda.orbit";
    /**
     * 实例列表
     */
    private List<Item> instances = new ArrayList<>();

    @Data
    public static class Item {
        /**
         * 唯一id名
         */
        private String keyName;

        /**
         * 切点表达式，如应用给Mapper的query方法：execution(* com..mapper.*.query*(..))
         */
        private String pointcutExpression;

        /**
         * 方法切面实现类
         */
        private Class<? extends OrbitAdvice> adviceClassName;

        /**
         * 切面实现属性注入
         */
        private Map<String, Object> props = new HashMap<>();
    }

}
