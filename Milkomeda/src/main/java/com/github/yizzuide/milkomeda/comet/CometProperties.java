package com.github.yizzuide.milkomeda.comet;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CometProperties
 *
 * @author yizzuide
 * @since 2.0.0
 * Create at 2019/12/12 18:04
 */
@Data
@ConfigurationProperties("milkomeda.comet")
public class CometProperties {
    /** 允许读取请求消息体（使用CometParam时必须开启） */
    private boolean  enableReadRequestBody = true;
}
