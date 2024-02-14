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

package com.github.yizzuide.milkomeda.universe.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * MilkomedaAutoConfiguration
 *
 * @author yizzuide
 * @since 0.2.1
 * @version 4.0.0
 * @see org.springframework.boot.context.annotation.ImportCandidates#load(Class, ClassLoader)
 * @see org.springframework.core.io.support.SpringFactoriesLoader
 * @see org.springframework.boot.SpringApplication
 * <br>
 * Create at 2019/04/12 11:29
 */
// Spring Boot 2.7: @AutoConfiguration should be used to annotate top-level autoconfiguration classes,
//    Configuration classes that are nested within or imported by an @AutoConfiguration class should continue to use @Configuration as before.
@AutoConfiguration
@Import(MilkomedaContextConfig.class)
public class MilkomedaAutoConfiguration {
}
