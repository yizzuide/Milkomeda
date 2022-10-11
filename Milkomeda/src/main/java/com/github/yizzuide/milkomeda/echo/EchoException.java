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

package com.github.yizzuide.milkomeda.echo;

import lombok.Getter;
import org.springframework.web.client.RestClientException;

/**
 * EchoException
 *
 * @author yizzuide
 * @since 1.13.0
 * <br>
 * Create at 2019/09/21 17:17
 */
public class EchoException extends RestClientException {
    private static final long serialVersionUID = -1012633504047012324L;
    @Getter
    private int code;
    @Getter
    private String body;

    public EchoException(String message) {
        super(message);
    }

    public EchoException(int code, String message) {
        super(message);
        this.code = code;
    }

    public EchoException(int code, String message, String body) {
        this(code, message);
        this.body = body;
    }
}
