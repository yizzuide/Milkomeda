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

package com.github.yizzuide.milkomeda.hydrogen.core;

import com.github.yizzuide.milkomeda.hydrogen.i18n.I18nMessages;

import javax.validation.Validator;

/**
 * HydrogenHolder
 *
 * @author yizzuide
 * @since 3.0.0
 * <br>
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
