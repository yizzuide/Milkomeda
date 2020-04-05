package com.github.yizzuide.milkomeda.hydrogen.validator;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * ValidatorProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/06 00:59
 */
@Data
@ConfigurationProperties("milkomeda.hydrogen.validator")
public class ValidatorProperties {
    /**
     * 启用验证器
     */
    private boolean enable = false;
    /**
     * 手机号正则表达式
     */
    private String regexPhone = "^((13[0-9])|(14[579])|(15([0-3]|[5-9]))|(166)|(17[0135678])|(18[0-9])|(19[8|9]))\\d{8}$";
}
