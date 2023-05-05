/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.universe.extend.annotation.Alias;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Whitelist;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * This request interceptor provide web XSS protection.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/05/01 04:28
 */
@Alias("xss")
public class CometXssRequestInterceptor extends AbstractRequestInterceptor {

    // 允许常用显示型html标签
    private static final Whitelist whitelist = Whitelist.basicWithImages();

    // 输出不格式化
    private static final Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);

    /**
     * White filed names is not prevent (only support form submit type).
     */
    @Setter
    private List<String> whiteFieldNames;

    static {
        // 标签可以带有style属性
        whitelist.addAttributes(":all", "style");
    }

    @Override
    protected String doReadRequest(HttpServletRequest request, String formName, String formValue, String body) {
        // 过滤Form表单项
        if (formValue != null) {
            // 匹配字段名，过滤白名单
            if (!CollectionUtils.isEmpty(whiteFieldNames)) {
                for (String whiteFieldName : whiteFieldNames) {
                    if (whiteFieldName.equals(formName)) {
                        return formValue;
                    }
                    if (whiteFieldName.startsWith("*") && whiteFieldName.endsWith(whiteFieldName.substring(1))) {
                        return formValue;
                    }
                    if (whiteFieldName.endsWith("*") && whiteFieldName.startsWith(whiteFieldName.substring(0, whiteFieldName.length() - 1))) {
                        return formValue;
                    }
                }
            }
            return Jsoup.clean(formValue, "", whitelist, outputSettings);
        }
        // 过滤请求body
        return Jsoup.clean(body, "", whitelist, outputSettings);
    }
}
