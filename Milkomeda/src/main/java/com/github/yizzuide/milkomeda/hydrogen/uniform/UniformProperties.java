package com.github.yizzuide.milkomeda.hydrogen.uniform;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * UniformProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/06 00:06
 */
@Data
@ConfigurationProperties("milkomeda.hydrogen.uniform")
public class UniformProperties {
    /**
     * 启用统一异常处理
     */
    private boolean enable = false;
    /**
     * 响应数据
     */
    private Map<String, Object> response = new HashMap<>();
}
