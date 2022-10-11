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

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

/**
 * Messages
 * 国际化文本获取
 *
 * @author yizzuide
 * @since 3.0.0
 * <br>
 * Create at 2019/08/01 17:13
 */
@Slf4j
@AllArgsConstructor
public class I18nMessages {

    private final MessageSource messageSource;

    /**
     * 获取国际化值
     * @param key   键
     * @return  String
     */
    public String get(String key) {
        return getWithParam(key);
    }

    /**
     * 根据请求设置的语言获取国际化值
     * @param key       键
     * @param params    占位参数值
     * @return  String
     */
    public String getWithParam(String key, String... params) {
        return getWithLocaleAndParam(key, LocaleContextHolder.getLocale(), params);
    }

    /**
     * 获取国际化值
     * @param key               键
     * @param localeIdentifier 国际化标识符，如: zh_CN
     * @param params           占位参数值
     * @return  String
     */
    public String getWithIdentifierAndParam(String key, String localeIdentifier, String... params) {
        return getWithLocaleAndParam(key, StringUtils.parseLocale(localeIdentifier), params);
    }

    /**
     * 获取国际化值
     * @param key       键
     * @param locale    Locale
     * @param params    占位参数值
     * @return  String
     */
    public String getWithLocaleAndParam(String key, Locale locale, String... params) {
        try {
            return messageSource.getMessage(key, params, locale);
        } catch (Exception e) {
            log.error("Hydrogen i18n get value error with key [{}], msg: {}", key, e.getMessage(), e);
            return key;
        }
    }

    /**
     * 设置固定的Locale
     * @param localeIdentifier    国际化标识符，如: zh_CN
     */
    public void setFixedLocale(String localeIdentifier) {
        setFixedLocale(StringUtils.parseLocale(localeIdentifier));
    }

    /**
     * 设置固定的Locale
     * @param locale    Locale
     */
    public void setFixedLocale(Locale locale) {
        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(WebContext.getRequest());
        if (localeResolver == null) {
            throw new IllegalStateException(
                    "No LocaleResolver found: not in a DispatcherServlet request?");
        }
        localeResolver.setLocale(WebContext.getRequest(), WebContext.getRequestAttributes().getResponse(), locale);
    }
}