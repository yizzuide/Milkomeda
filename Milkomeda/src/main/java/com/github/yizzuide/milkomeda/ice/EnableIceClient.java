package com.github.yizzuide.milkomeda.ice;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * EnableIceClient
 * 启用Ice客户端，配置项 <code>milkomeda.ice.enable-task</code> 的设置值将不再有效
 *
 * @author yizzuide
 * @since 1.15.2
 * Create at 2019/11/21 11:20
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@Import({IceClientConfig.class, IceScheduleConfig.class})
public @interface EnableIceClient {
}
