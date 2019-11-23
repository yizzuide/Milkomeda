package com.github.yizzuide.milkomeda.ice;

import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.lang.annotation.*;

/**
 * EnableIceServer
 * 启用Ice服务端，配置项 <code>milkomeda.ice.enable-job-timer</code> 设置值将不再有效
 *
 * @author yizzuide
 * @since 1.15.2
 * @version 1.16.0
 * Create at 2019/11/21 10:57
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Inherited
@EnableScheduling
@Import(IceServerConfig.class)
public @interface EnableIceServer {
}
