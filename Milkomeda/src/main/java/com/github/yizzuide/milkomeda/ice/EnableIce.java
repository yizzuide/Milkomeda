package com.github.yizzuide.milkomeda.ice;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableIce
 * 启用Ice，该注解是<code>@EnableIceServer</code>和<code>@EnableIceClient</code>的合并注解，
 * 需要在配置文件中配置成你想要的启动类型：<br>
 *     <dl>
 *        <dt>配置为服务端</dt>
 *        <dd>
 *            <pre class="code">
 *              # 延迟队列分桶数量（默认为3）
 *              delay-bucket-count: 2
 *              # 消费执行超时时间（默认5000ms）
 *              ttr: 10000
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
 *               # 消费轮询间隔（默认5000ms）
 *               task-execute-rate: 2000
 *            </pre>
 *        </dd>
 *        <dt>配置为单体应用（服务端+消费端）</dt>
 *        <dd>
 *            <pre class="code">
 *               # 延迟队列分桶数量（默认为3）
 *               delay-bucket-count: 2
 *               # 消费执行超时时间（默认5000ms）
 *               ttr: 10000
 *               # 重试时添加延迟增长因子（默认为1，计算公式：delay * (retryCount++) * factor）
 *               retry-delay-multi-factor: 2
 *               # 开启Task功能，开发者就可以通过注解方式接收topic消息
 *               enable-task: true
 *               # 最大消费个数（默认为10）
 *               task-topic-pop-max-size: 5
 *               # 消费轮询间隔（默认5000ms）
 *               task-execute-rate: 2000
 *            </pre>
 *        </dd>
 *     </dl>
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/16 17:24
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import({IceConfig.class, IceScheduleConfig.class})
public @interface EnableIce {
}
