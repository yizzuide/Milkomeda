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
