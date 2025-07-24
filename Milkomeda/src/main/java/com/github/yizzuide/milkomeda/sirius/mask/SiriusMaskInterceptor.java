/*
 * Copyright (c) 2025 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sirius.mask;

import com.github.yizzuide.milkomeda.util.StringEncryptUtil;
import lombok.AllArgsConstructor;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;

import java.sql.Statement;
import java.util.Arrays;
import java.util.List;

/**
 * Sensitive field masking mybatis interceptor.
 *
 * @version 4.0.0
 * @author yizzuide
 * Create at 2025/07/24 13:43
 */

@AllArgsConstructor
@Intercepts({
    @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
public class SiriusMaskInterceptor implements Interceptor {

    private final SiriusMaskProperties maskProperties;

    private final List<MaskFilter> maskFilters;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object result = invocation.proceed();
        if (!maskProperties.isEnable()) {
            return result;
        }
        if (maskFilters != null) {
            boolean useMask = false;
            for (MaskFilter maskFilter : maskFilters) {
                if (!maskFilter.filter(result)) {
                    useMask = true;
                    break;
                }
            }
            if (!useMask) {
                return result;
            }
        }
        if (result instanceof List) {
            for (Object obj : (List<?>) result) {
                maskFields(obj);
            }
        } else {
            maskFields(result);
        }
        return result;
    }

    private void maskFields(Object obj) {
        if (obj == null) return;
        Class<?> clazz = obj.getClass();
        Arrays.stream(clazz.getDeclaredFields())
                .filter(f -> f.isAnnotationPresent(MaskField.class))
                .forEach(f -> {
                    try {
                        f.setAccessible(true);
                        Object value = f.get(obj);
                        if (value != null) {
                            String masked = StringEncryptUtil.masking(value.toString(), maskProperties.getMaskChar());
                            f.set(obj, masked);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(String.format("字段[%s]脱敏失败", f.getName()), e);
                    }
                });
    }
}
