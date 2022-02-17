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

package com.github.yizzuide.milkomeda.jupiter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * JupiterProperties
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/19 22:13
 */
@Data
@ConfigurationProperties("milkomeda.jupiter")
public class JupiterProperties {
    /**
     * 规则引擎类型（目前仅支持SCOPE）
     */
    private JupiterRuleEngineType type = JupiterRuleEngineType.SCOPE;

    /**
     * 自定义规则引擎
     */
    private Class<? extends JupiterRuleEngine> ruleEngineClazz;

    /**
     * 是否将匹配表达式里的结果字段下划线转驼峰
     */
    private boolean matchCamelCase = false;

    /**
     * 规则实例列表
     */
    private Map<String, Rule> rules;

    /**
     * 规则实例
     */
    @Data
    public static class Rule {
        /**
         * 规则列表
         */
        private Map<String, RuleItem> ruleItems;
    }

    /**
     * 规则子项
     */
    @Data
    public static class RuleItem {
        /**
         * 数据领域源（表名）
         */
        private String domain;

        /**
         * 源字段抽取
         */
        private String fields = "*";

        /**
         * 结果是否有多条记录
         */
        private boolean multi = true;

        /**
         * 过滤条件
         */
        private String filter;

        /**
         * 规则匹配表达式
         */
        private String match;

        /**
         * 匹配语法
         */
        private JupiterCompilerType syntax = JupiterCompilerType.EL;
    }
}
