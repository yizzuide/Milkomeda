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

import com.github.yizzuide.milkomeda.comet.core.AbstractResponseInterceptor;
import com.github.yizzuide.milkomeda.universe.extend.annotation.AliasBinder;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlResponseOutput;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Uniform implementation of response interceptor. It works when response type is subclass of {@link ResultVO}.
 *
 * @since 3.14.0
 * @version 3.20.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/10 17:57
 */
@Slf4j
@AliasBinder(value = "uniform", autoload = true)
public class UniformResponseInterceptor extends AbstractResponseInterceptor {
    @Override
    protected Object doResponse(HttpServletResponse response, Object body) {
        if (body instanceof ResultVO<?>) {
            ResultVO<?> resultVO = (ResultVO<?>) body;
            Map<String, Object> source = new HashMap<>(8);
            source.put(YmlResponseOutput.CODE, resultVO.getCode() == null ?
                    UniformHolder.getProps().getDefaultSuccessCode() : resultVO.getCode());
            source.put(YmlResponseOutput.MESSAGE, resultVO.getMessage());
            source.put(YmlResponseOutput.DATA, resultVO.getData());
            return UniformHandler.matchStatusResult(response, source).getT2();
        }
        return null;
    }
}
