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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.Type;

/**
 * TypeUtil
 *
 * @since 1.10.0
 * @version 2.1.0
 * @author yizzuide
 * <br />
 * Create at 2019/07/02 13:23
 */
public class TypeUtil {
    /**
     * Class 转 TypeReference
     * @param clazz 源class
     * @param <T>   类泛型
     * @return  TypeReference
     */
    public static <T> TypeReference<T> class2TypeRef(Class<T> clazz) {
        return new TypeReference<T>(){
            @Override
            public Type getType() {
                return clazz;
            }
        };
    }

    /**
     * TypeReference 转 Class
     * @param typeRef   TypeReference
     * @return  Class
     */
    public static Class<?> type2Class(TypeReference<?> typeRef) {
        return type2JavaType(typeRef.getType()).getRawClass();
    }

    /**
     * Type 转 Class
     * @param type   TypeReference
     * @return  Class
     */
    public static Class<?> type2Class(Type type) {
        return type2JavaType(type).getRawClass();
    }

    /**
     * Type 转 JavaType
     * @param type Type
     * @return JavaType
     */
    public static JavaType type2JavaType(Type type) {
        return TypeFactory.defaultInstance().constructType(type);
    }
}
