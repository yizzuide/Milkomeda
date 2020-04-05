package com.github.yizzuide.milkomeda.hydrogen.i18n;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * I18nProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/06 01:08
 */
@Data
@ConfigurationProperties("milkomeda.hydrogen.i18n")
public class I18nProperties {
    /**
     * 启用国际化
     */
    private boolean enable = false;

    /**
     * 请求语言设置参数名，参数值如：zh_CN （language_country）
     */
    private String query = "lang";

    /*
     * 记录语言选择到会话（API项目设置为false，后端管理项目保存默认）
     */
    // private boolean useSessionQuery = true;

    /*
     * 设置请求语言设置到会话的key（如无特殊情况，不需要修改）
     */
    // private String querySessionName = "hydrogen_i18n_language_session";
}
