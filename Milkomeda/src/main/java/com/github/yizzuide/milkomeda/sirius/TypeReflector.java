/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import org.apache.ibatis.reflection.Reflector;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Cached reflect for class
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2024/03/10 17:03
 */
public final class TypeReflector {

    private static final Map<Class<?>, Reflector> cachedReflectorMap = new HashMap<>();

    public static <T> void setProps(T target, String property, Object ...values) {
        try {
            Reflector reflector = cachedReflectorMap.putIfAbsent(target.getClass(), new Reflector(target.getClass()));
            if (reflector == null) {
                reflector = cachedReflectorMap.get(target.getClass());
            }
            reflector.getSetInvoker(property).invoke(target, values);
        } catch (ReflectiveOperationException e) {
            throw ExceptionUtils.mpe("Error: Cannot write property in %s.  Cause:", e, target.getClass().getSimpleName());
        }
    }

    public static <T> Field[] getFields(Class<T> type) {
        Reflector reflector = cachedReflectorMap.putIfAbsent(type, new Reflector(type));
        if (reflector == null) {
            reflector = cachedReflectorMap.get(type);
        }
        return reflector.getType().getDeclaredFields();
    }
}
