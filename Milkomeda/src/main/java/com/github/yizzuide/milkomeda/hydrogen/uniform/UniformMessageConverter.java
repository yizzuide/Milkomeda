/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlResponseOutput;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Uniform message converter to convert to {@link UniformResult}.
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2025/06/27 09:11
 */
public class UniformMessageConverter extends AbstractHttpMessageConverter<Object> {

    private final ObjectMapper objectMapper;

    public UniformMessageConverter(ObjectMapper objectMapper) {
        super(MediaType.APPLICATION_JSON);
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean supports(@NotNull Class<?> clazz) {
        // ResultVO 和 void 不转换
        return !ResultVO.class.isAssignableFrom(clazz) && !Void.TYPE.isAssignableFrom(clazz);
    }

    @Override
    protected Object readInternal(@NotNull Class<?> clazz, @NotNull HttpInputMessage inputMessage) throws IOException, HttpMessageNotReadableException {
        // 不处理输入
        return null;
    }

    @Override
    protected void writeInternal(@NotNull Object body, @NotNull HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        outputMessage.getHeaders().put("Content-Type", List.of("application/json;charset=UTF-8"));
        ResultVO<?> resultVO =  UniformResult.ok(body);
        Map<String, Object> source = new HashMap<>(8);
        source.put(YmlResponseOutput.CODE, resultVO.getCode() == null ?
                UniformHolder.getProps().getDefaultSuccessCode() : resultVO.getCode());
        source.put(YmlResponseOutput.MESSAGE, resultVO.getMessage());
        source.put(YmlResponseOutput.DATA, resultVO.getData());
        outputMessage.getBody().write(objectMapper.writeValueAsString(source).getBytes(StandardCharsets.UTF_8));
    }
}
