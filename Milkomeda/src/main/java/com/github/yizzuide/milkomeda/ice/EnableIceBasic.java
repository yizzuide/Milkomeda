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

package com.github.yizzuide.milkomeda.ice;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableIceBasic
 * 基本Ice Job Pool存储，只提供Job的Push功能，可用于只添加延迟Job的服务，
 * 配置项 <code>enableJobTimer</code> 和 <code>enableTask</code> 的开启将失效 <br>
 *
 * 只需要配置两项即可，如果使用默认值，则不需要配置，以下是定制配置的例子：
 * <pre class="code">
 *   # 延迟队列分桶数量（默认为3）
 *   delay-bucket-count: 2
 *   # 消费执行超时时间（默认5000ms）
 *   ttr: 10s
 * </pre>
 *
 * 注意：如果在同一延迟业务，<code>delay-bucket-count</code> 在 {@link EnableIceBasic} 与 {@link EnableIceServer}
 * 或 {@link EnableIceBasic} 与 {@link EnableIce} 里保持一致！
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/09 11:31
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import(IceBasicConfig.class)
public @interface EnableIceBasic {
}
