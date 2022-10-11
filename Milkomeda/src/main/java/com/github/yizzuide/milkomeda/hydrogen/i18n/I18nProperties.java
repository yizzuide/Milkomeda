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

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * I18nProperties
 *
 * @author yizzuide
 * @since 3.0.0
 * <br>
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
