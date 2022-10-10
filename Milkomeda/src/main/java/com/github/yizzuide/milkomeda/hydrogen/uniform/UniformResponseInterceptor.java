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
import com.github.yizzuide.milkomeda.universe.lang.Tuple;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlResponseOutput;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.util.FastByteArrayOutputStream;

import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Uniform impl of response wrapper interceptor.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br />
 * Create at 2022/10/10 17:57
 */
@Slf4j
public class UniformResponseInterceptor implements CometResponseInterceptor {
    @Override
    public boolean writeToResponse(FastByteArrayOutputStream outputStream, HttpServletResponse wrapperResponse, HttpServletResponse rawResponse, Object body) {
        if (body instanceof ResultVO) {
            ResultVO<?> resultVO = (ResultVO<?>) body;
            Map<String, Object> source = new HashMap<>(5);
            source.put(YmlResponseOutput.CODE, resultVO.getCode());
            source.put(YmlResponseOutput.MESSAGE, resultVO.getMessage());
            source.put(YmlResponseOutput.DATA, resultVO.getData());
            try {
                Tuple<Map<String, Object>, Map<String, Object>> mapTuple = UniformHandler.matchStatusResult(rawResponse, source);
                // has config 200?
                if (mapTuple.getT1() == null || mapTuple.getT1().size() == 0) {
                    return false;
                }
                Map<String, Object> result = mapTuple.getT2();
                String content = JSONUtil.serialize(result);
                // reset content and length
                byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
                wrapperResponse.resetBuffer();
                wrapperResponse.setContentLength(bytes.length);
                outputStream.write(bytes);
                rawResponse.setContentLength(outputStream.size());
                // winter to response
                outputStream.writeTo(rawResponse.getOutputStream());
                return true;
            } catch (Exception e) {
                log.error("uniform response error with msg: {}", e.getMessage());
                return false;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
