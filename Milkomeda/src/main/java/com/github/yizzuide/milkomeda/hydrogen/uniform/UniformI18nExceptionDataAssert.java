/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.hydrogen.uniform;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;
import com.github.yizzuide.milkomeda.util.PlaceholderResolver;

/**
 * Exception message handling implemented in combine with MessageSource. <br>
 * Before use this class, you must enable {@link com.github.yizzuide.milkomeda.hydrogen.core.EnableHydrogen} and add the following configuration:
 * <pre>
 *     milkomeda.hydrogen.i18n.enable = true
 * </pre>
 *
 * @author yizzuide
 * Create at 2022/08/28 00:44
 * @since 3.13.0
 */
public interface UniformI18nExceptionDataAssert extends UniformExceptionDataAssert {

    String MESSAGE_SOURCE_TOKEN = "ms.${";

    @Override
    default String formatMessage(String msg, Object... args) {
        if (msg.contains(MESSAGE_SOURCE_TOKEN)) {
            PlaceholderResolver placeholderResolver = PlaceholderResolver.getResolver(MESSAGE_SOURCE_TOKEN);
            msg = placeholderResolver.resolveByRule(msg, key -> HydrogenHolder.getI18nMessages().get(key));
        }
        return UniformExceptionDataAssert.super.formatMessage(msg, args);
    }
}
