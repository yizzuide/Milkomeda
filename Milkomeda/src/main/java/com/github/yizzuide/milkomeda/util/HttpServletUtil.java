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

package com.github.yizzuide.milkomeda.util;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpServletUtil
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 3.0.0
 * <br>
 * Create at 2019/04/11 20:10
 */
public class HttpServletUtil {
    /**
     * 获取请求参数 转换成 json字符串
     *
     * @param request HttpServletRequest
     * @return json字符串
     */
    public static String getRequestData(HttpServletRequest request) {
        Map<String, Object> inputs = new HashMap<>();
        for (Map.Entry<String, String[]> paramEntry : request.getParameterMap().entrySet()) {
            String[] value = paramEntry.getValue();
            if (value == null) {
                inputs.put(paramEntry.getKey(), "");
            } else if (value.length == 1) {
                inputs.put(paramEntry.getKey(), value[0]);
            } else {
                inputs.put(paramEntry.getKey(), value);
            }
        }
        return JSONUtil.serialize(inputs);
    }
}
