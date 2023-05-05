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

package com.github.yizzuide.milkomeda.hydrogen.uniform;

import com.github.yizzuide.milkomeda.comet.core.CometResponseInterceptor;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlResponseOutput;

import java.util.HashMap;
import java.util.Map;

/**
 * This interface is specification as a response. The default implementation is {@link UniformResult}, its used with
 * {@link CometResponseInterceptor} of comet module which default implementation is {@link UniformResponseInterceptor}.
 * If used this type to declare response, must config the follow:
 * <pre>
 *     1. Enable response wrapper with config: <code>milkomeda.comet.enable-read-response-body=true</code>.
 *     2. Add response interceptor with config: <code>milkomeda.comet.response-interceptors.uniform.enable=true</code>.
 *     3. (Optional) If you need change the response field name, such as change `message` to `msg` with config: <code>milkomeda.hydrogen.uniform.response.200.message[msg]=""</code>.
 * </pre>
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/10 18:38
 */
public interface ResultVO<T> {
    /**
     * Code field type.
     */
    enum CodeType {
        INT,
        STRING
    }

    /**
     * Code field of response body.
     * @return  String
     */
    String getCode();
    /**
     * Message field of response body.
     * @return  String
     */
    String getMessage();
    /**
     * Data field of response body.
     * @return  T
     */
    T getData();

    /**
     * Convert to standard response map with filed: code, message, data.
     * @return Map
     */
    default Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(6);
        map.put(YmlResponseOutput.CODE, getCode());
        map.put(YmlResponseOutput.MESSAGE, getMessage());
        map.put(YmlResponseOutput.DATA, getData());
        return map;
    }
}
