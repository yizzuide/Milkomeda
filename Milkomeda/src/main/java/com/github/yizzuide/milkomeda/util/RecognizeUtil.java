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

package com.github.yizzuide.milkomeda.util;

import org.springframework.util.ClassUtils;

import java.beans.Introspector;

/**
 * 类型识别工具类
 *
 * @author yizzuide
 * @since 1.13.9
 * @version 1.15.0
 * <br>
 * Create at 2019/10/29 15:49
 */
public class RecognizeUtil {
    /**
     * 是否是复合类型
     * @param obj   源数据
     * @return  复合类型返回true
     */
    public static boolean isCompoundType(Object obj) {
        return !(obj instanceof Byte || obj instanceof Boolean || obj instanceof CharSequence ||
                obj instanceof Short || obj instanceof Integer || obj instanceof Long || obj instanceof Double || obj instanceof Float);
    }

    /**
     * 是否是复合类型
     * @param cls   类
     * @return  复合类型返回true
     */
    public static boolean isCompoundType(Class<?> cls) {
        return !(cls == Byte.class || cls == Boolean.class || Character.class.isAssignableFrom(cls) ||
                cls == Short.class || cls == Integer.class || cls == Long.class || cls == Double.class || cls == Float.class);
    }

    /**
     * Get bean name from class.
     * @param beanClass bean class
     * @return  bean name
     * @since 3.15.0
     */
    public static String getBeanName(Class<?> beanClass) {
        String shortClassName = ClassUtils.getShortName(beanClass);
        return Introspector.decapitalize(shortClassName);
    }
}
