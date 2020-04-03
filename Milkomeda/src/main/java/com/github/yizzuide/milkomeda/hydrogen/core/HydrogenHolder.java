package com.github.yizzuide.milkomeda.hydrogen.core;

import com.github.yizzuide.milkomeda.hydrogen.i18n.I18nMessages;
import org.springframework.util.PathMatcher;
import org.springframework.web.util.UrlPathHelper;

import javax.validation.Validator;

/**
 * HydrogenHolder
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/26 20:25
 */
public class HydrogenHolder {
    /**
     * 配置数据
     */
    private static HydrogenProperties props;

    /**
     * 验证器
     */
    private static Validator validator;

    /**
     * 国际化
     */
    private static I18nMessages i18nMessages;

    /**
     * 路径匹配器
     */
    private static PathMatcher mvcPathMatcher;

    /**
     * URL路径帮助类
     */
    private static UrlPathHelper urlPathHelper;

    public static void setProps(HydrogenProperties props) {
        HydrogenHolder.props = props;
    }

    public static HydrogenProperties getProps() {
        return props;
    }

    public static void setValidator(Validator validator) {
        HydrogenHolder.validator = validator;
    }

    public static Validator getValidator() {
        return validator;
    }

    public static void setI18nMessages(I18nMessages i18nMessages) {
        HydrogenHolder.i18nMessages = i18nMessages;
    }

    public static I18nMessages getI18nMessages() {
        return i18nMessages;
    }

    public static void setMvcPathMatcher(PathMatcher mvcPathMatcher) {
        HydrogenHolder.mvcPathMatcher = mvcPathMatcher;
    }

    public static PathMatcher getMvcPathMatcher() {
        return mvcPathMatcher;
    }

    public static void setUrlPathHelper(UrlPathHelper urlPathHelper) {
        HydrogenHolder.urlPathHelper = urlPathHelper;
    }

    public static UrlPathHelper getUrlPathHelper() {
        return urlPathHelper;
    }

}
