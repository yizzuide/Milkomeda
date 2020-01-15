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
