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

import com.github.yizzuide.milkomeda.universe.parser.yml.YmlResponseOutput;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Uniformed result view object.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/10 16:24
 */
@Data
public class UniformResult<T> implements ResultVO<T> {

    private String code;

    private String message;

    private T data;

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>(5);
        map.put(YmlResponseOutput.CODE, getCode());
        map.put(YmlResponseOutput.MESSAGE, getMessage());
        map.put(YmlResponseOutput.DATA, getData());
        return map;
    }

    /**
     * Return success.
     * @param data  success data
     * @param <T>   data type
     * @return  ResultVO
     */
    public static <T> ResultVO<T> ok(T data) {
        UniformResult<T> resultVo = new UniformResult<>();
        resultVo.setData(data);
        return resultVo;
    }

    /**
     * Return failure.
     * @param code      failure code
     * @param message   failure message
     * @param <T>   data type
     * @return  ResultVO
     */
    public static <T> ResultVO<T> error(String code, String message) {
        UniformResult<T> resultVo = new UniformResult<>();
        resultVo.setCode(code);
        resultVo.setMessage(message);
        return resultVo;
    }
}
