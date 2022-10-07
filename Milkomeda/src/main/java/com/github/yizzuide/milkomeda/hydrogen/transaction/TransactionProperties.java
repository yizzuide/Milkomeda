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

package com.github.yizzuide.milkomeda.hydrogen.transaction;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * TransactionProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * <br />
 * Create at 2020/04/06 00:51
 */
@Data
@ConfigurationProperties("milkomeda.hydrogen.transaction")
public class TransactionProperties {
    /**
     * 启用AOP事务
     */
    private boolean enable = false;
    /**
     * 切点表达式
     */
    private String pointcutExpression = "execution(* com..service.*.*(..))";
    /**
     * 事务超时回滚（默认单位：s。-1：不设置超时回滚）
     */
    @DurationUnit(ChronoUnit.SECONDS)
    private Duration rollbackWhenTimeout = Duration.ofSeconds(-1);
    /**
     * 指定异常类回滚
     */
    private List<Class<? extends Exception>> rollbackWhenException = Collections.singletonList(Exception.class);
    /**
     * 只读事务方法前辍
     */
    private List<String> readOnlyPrefix = Arrays.asList("get*", "query*", "find*", "select*", "list*", "count*", "is*");
    /**
     * 追加只读事务方法前辍
     */
    private List<String> readOnlyAppendPrefix;
}
