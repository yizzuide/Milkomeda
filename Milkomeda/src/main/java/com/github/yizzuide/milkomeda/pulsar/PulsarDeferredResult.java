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

package com.github.yizzuide.milkomeda.pulsar;

import lombok.Data;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * PulsarDeferredResult
 * DeferredResult的简单代理
 *
 * @author yizzuide
 * @since  0.1.0
 * @version 1.4.0
 * <br />
 * Create at 2019/03/30 00:03
 */
@Data
public class PulsarDeferredResult {
    /**
     * 唯一标识
     */
    private String deferredResultID;

    /**
     * 源DeferredResult
     */
    private DeferredResult<Object> deferredResult;

    /**
     * 返回包装的DeferredResult
     * @return DeferredResult
     */
    public DeferredResult<Object> value() {
        return deferredResult;
    }
}
