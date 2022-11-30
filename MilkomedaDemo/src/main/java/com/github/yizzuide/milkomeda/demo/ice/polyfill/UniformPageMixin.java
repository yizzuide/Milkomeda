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

package com.github.yizzuide.milkomeda.demo.ice.polyfill;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformPage;
import org.springframework.boot.jackson.JsonMixin;

/**
 * UniformPageMixin
 *
 * @author yizzuide
 * <br>
 * Create at 2022/11/30 19:06
 */
// Springboot 2.7: Spring Boot的Jackson自动配置将扫描应用程序的包以查找带有@JsonMixin注释的类，并将它们注册到自动配置的ObjectMapper，
//  注册动作由Spring Boot的JsonMixinModule执行
@JsonMixin(UniformPage.class)
public abstract class UniformPageMixin {
    // 混入属性并定制的新名称
    @JsonProperty("pageTotalSize")
    Long pageCount;
}
