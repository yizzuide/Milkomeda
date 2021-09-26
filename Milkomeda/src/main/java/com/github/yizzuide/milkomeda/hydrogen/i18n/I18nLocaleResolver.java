/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.hydrogen.i18n;

import org.springframework.lang.NonNull;
import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * CustomLocaleResolver
 *
 * @author yizzuide
 * @since 3.0.0
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

    @NonNull
    @Override
    public Locale resolveLocale(@NonNull HttpServletRequest request) {
        // 是否有固定的设置
        Locale defaultLocale = getDefaultLocale();
        if (defaultLocale != null) {
            return defaultLocale;
        }

        // 设置本地语言
        return Locale.getDefault();
    }

    @Override
    public void setLocale(@NonNull HttpServletRequest request, HttpServletResponse response, Locale locale) {
        setDefaultLocale(locale);
    }
}
