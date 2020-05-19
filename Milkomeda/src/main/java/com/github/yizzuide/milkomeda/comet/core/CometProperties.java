package com.github.yizzuide.milkomeda.comet.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CometProperties
 *
 * @author yizzuide
 * @since 2.0.0
 * @version 3.5.0
 * Create at 2019/12/12 18:04
 */
@Data
@ConfigurationProperties("milkomeda.comet")
public class CometProperties {

    /**
     * 允许开启请求包装类读取请求消息体（使用 {@link CometParam } 时必须开启）
     */
    private boolean enableReadRequestBody = false;

    /**
     * 允许开启响应包装类读取响应消息体，依赖开启 {@link CometProperties#enableReadRequestBody}
     * （使用 Collector 时，如果通过注入HttpServletResponse直接写出流则必须开启）
     * @see CometProperties#enableReadRequestBody
     */
    private boolean enableReadResponseBody = false;

    /**
     * 成功状态码
     */
    private String statusSuccessCode = "1";

    /**
     * 失败状态码
     */
    private String statusFailCode = "2";
}
