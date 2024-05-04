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

package com.github.yizzuide.milkomeda.universe.extend.annotation;

import com.github.yizzuide.milkomeda.universe.extend.web.handler.NamedHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Alias bind bean in spring context.
 *
 * @since 3.15.0
 * @version 4.0.0
 * @author yizzuide
 * <br>
 * Create at 2023/05/05 22:10
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class AliasWrapper<T> {

    /**
     * Alias or bean name (in favor of {@link NamedHandler#handlerName()} maybe return bean name) link to bean.
     */
    private String name;

    /**
     * Bean name in spring context.
     */
    private String beanName;

    /**
     * Bean instance.
     */
    private T bean;

    /**
     * Whether used to load automatically.
     * @since 4.0.0
     */
    private boolean autoload;
}
