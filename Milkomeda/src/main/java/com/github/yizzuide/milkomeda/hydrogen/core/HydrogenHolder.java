package com.github.yizzuide.milkomeda.hydrogen.core;

import com.github.yizzuide.milkomeda.hydrogen.i18n.I18nMessages;

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
     * 验证器
     */
    private static Validator validator;

    /**
     * 国际化
     */
    private static I18nMessages i18nMessages;

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
}
