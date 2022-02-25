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

import lombok.Builder;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * OrbitNode
 * 用于外部扩展的切面节点
 *
 * @author yizzuide
 * @since 3.13.0
 * Create at 2022/02/26 00:58
 */
@Getter
@Builder
public class OrbitNode {
    /**
     * 切面节点唯一标识
     */
    private String id;
    /**
     * 切面节点表达式
     */
    private String pointcutExpression;
    /**
     * 切面实现类
     */
    private Class<? extends OrbitAdvice> adviceClass;
    /**
     * 切面实现属性配置
     */
    private Map<String, Object> props;

    /**
     * 添加单个KV
     * @param key   键
     * @param value 值
     * @return  OrbitNode
     */
    public OrbitNode putPropKV(String key, Object value) {
        if (props == null) {
            props = new HashMap<>();
        }
        props.put(key, value);
        return this;
    }
}
