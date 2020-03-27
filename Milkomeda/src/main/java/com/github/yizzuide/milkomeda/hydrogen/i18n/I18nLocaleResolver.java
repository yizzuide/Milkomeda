package com.github.yizzuide.milkomeda.hydrogen.i18n;

import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * CustomLocaleResolver
 *
 * @author yizzuide
 * @since 2.8.0
 * @see org.springframework.web.servlet.i18n.LocaleChangeInterceptor
 * @see org.springframework.web.servlet.i18n.AbstractLocaleContextResolver
 * @see org.springframework.web.servlet.i18n.FixedLocaleResolver
 * @see org.springframework.web.servlet.i18n.SessionLocaleResolver
 * Create at 2019/08/01 17:04
 */
public class I18nLocaleResolver extends AbstractLocaleResolver {

    // 不使用LocaleChangeInterceptor时的自定义方案
    /*@Override
    public Locale resolveLocale(HttpServletRequest request) {
        // 是否有固定的设置
        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale != null) {
            return defaultLocale;
        }

        // 从请求中获取
        String lang = request.getParameter(HydrogenHolder.getProps().getI18n().getQuery());
        Locale locale = null;
        if (!StringUtils.isEmpty(lang)) {
            locale = StringUtils.parseLocale(lang);

            // 将国际化语言保存到session
            if (HydrogenHolder.getProps().getI18n().isUseSessionQuery()) {
                HttpSession session = request.getSession();
                session.setAttribute(HydrogenHolder.getProps().getI18n().getQuerySessionName(), locale);
            }
            return locale;
        }

        // 如果会话中有值就返回
        if (HydrogenHolder.getProps().getI18n().isUseSessionQuery()) {
            locale = (Locale) request.getSession().getAttribute(HydrogenHolder.getProps().getI18n().getQuerySessionName());
        }

        // 设置本地默认
        if (locale == null) {
            locale = Locale.getDefault();
        }
        return locale;
    }*/

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // 是否有固定的设置
        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale != null) {
            return defaultLocale;
        }

        // 设置本地语言
        return Locale.getDefault();
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        setDefaultLocale(locale);
    }
}
