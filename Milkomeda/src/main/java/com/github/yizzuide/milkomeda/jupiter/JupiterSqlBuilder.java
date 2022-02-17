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

/**
 * JupiterSqlBuilder
 *
 * @author yizzuide
 * @since 3.5.0
 * @version 3.12.10
 * Create at 2020/05/20 15:05
 */
public class JupiterSqlBuilder {
    /**
     * 构建查询SQL
     * @param ruleItem  规则项
     * @param filter    过滤条件
     * @return  sql
     */
    public static String build(JupiterRuleItem ruleItem, String filter) {
        StringBuilder sqlBuilder = new StringBuilder("select " + ruleItem.getFields() + " from " + ruleItem.getDomain());
        if (filter != null) {
            sqlBuilder.append(" where ");
            filter = filter.replace("&&", " and ")
                            .replace("||", " or ");
            sqlBuilder.append(filter);
        }
        if (!ruleItem.isMulti()) {
            sqlBuilder.append(" limit 1");
        }
        return sqlBuilder.toString();
    }
}
