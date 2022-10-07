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
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.annotation.*;

/**
 * EnableIce
 * <br>
 * 需要在配置文件中配置成你想要的启动类型：<br>
 *     <dl>
 *        <dt>配置为服务端</dt>
 *        <dd>
 *            <pre class="code">
 *              # 延迟队列分桶数量（默认为3）
 *              delay-bucket-count: 2
 *              # 消费执行超时时间（默认30s）
 *              ttr: 20s
 *              # 重试时添加延迟增长因子（默认为1，计算公式：delay * (retryCount++) * factor）
 *              retry-delay-multi-factor: 2
 *            </pre>
 *        </dd>
 *        <dt>配置为消费端</dt>
 *        <dd>
 *            <pre class="code">
 *               # 禁止启动Job作业
 *               enable-job-timer: false
 *               # 开启Task功能，开发者就可以通过注解方式接收topic消息
 *               enable-task: true
 *               # 最大消费个数（默认为10）
 *               task-topic-pop-max-size: 5
 *               # 消费轮询间隔（默认5s）
 *               task-execute-rate: 2s
 *            </pre>
 *        </dd>
 *        <dt>配置为单体应用（服务端+消费端）</dt>
 *        <dd>
 *            <pre class="code">
 *               # 延迟队列分桶数量（默认为3）
 *               delay-bucket-count: 2
 *               # 消费执行超时时间（默认30s）
 *               ttr: 20s
 *               # 重试时添加延迟增长因子（默认为1，计算公式：delay * (retryCount++) * factor）
 *               retry-delay-multi-factor: 2
 *               # 开启Task功能，开发者就可以通过注解方式接收topic消息
 *               enable-task: true
 *               # 最大消费个数（默认为10）
 *               task-topic-pop-max-size: 5
 *               # 消费轮询间隔（默认5s）
 *               task-execute-rate: 2s
 *            </pre>
 *        </dd>
 *        <dt>配置为数据发送端</dt>
 *        <dd>
 *            <pre>
 *               # 禁止启动Job作业
 *               enable-job-timer: false
 *               # 延迟队列分桶数量（默认为3）
 *               delay-bucket-count: 2
 *               # 消费执行超时时间（默认5000ms）
 *               ttr: 10s
 *           </pre>
 *        </dd>
 *     </dl>
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 1.16.0
 * <br />
 * Create at 2019/11/16 17:24
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@EnableScheduling
@Import({IceConfig.class, IceScheduleConfig.class})
public @interface EnableIce {
}
