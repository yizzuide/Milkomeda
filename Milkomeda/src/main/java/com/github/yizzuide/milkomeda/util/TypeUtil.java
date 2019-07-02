package com.github.yizzuide.milkomeda.util;

import com.fasterxml.jackson.core.type.TypeReference;

import java.lang.reflect.Type;

/**
 * TypeUtil
 *
 * @since 1.10.0
 * @author yizzuide
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
}
