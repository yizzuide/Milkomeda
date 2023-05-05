/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.universe.extend.annotation.Alias;

import javax.servlet.http.HttpServletRequest;
import java.util.regex.Pattern;

/**
 * This request interceptor provide web sql inject protection.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/05/01 21:21
 */
@Alias("sql-inject")
public class CometSqlInjectRequestInterceptor extends AbstractCometRequestInterceptor {

    private static final String SQL_REG_EXP = "\\b(and|or)\\b.{1,6}?(=|>|<|\\bin\\b|\\blike\\b)|\\/\\*.+?\\*\\/|<\\s*script\\b|\\bEXEC\\b|UNION.+?SELECT|UPDATE.+?SET|INSERT\\s+INTO.+?VALUES|(SELECT|DELETE).+?FROM|(CREATE|ALTER|DROP|TRUNCATE)\\s+(TABLE|DATABASE)";

    @Override
    protected String doReadRequest(HttpServletRequest request, String formName, String formValue, String body) {
        String value = formValue == null ? body : formValue;
        if (value == null) {
            return value;
        }
        Pattern sqlPattern = Pattern.compile(SQL_REG_EXP, Pattern.CASE_INSENSITIVE);
        if (sqlPattern.matcher(value.toLowerCase()).find()) {
            throw new RuntimeException("Detected SQL injection with value: " + value);
        }
        return value;
    }
}
