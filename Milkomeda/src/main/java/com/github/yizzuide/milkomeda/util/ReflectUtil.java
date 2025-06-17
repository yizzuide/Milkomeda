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

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * ReflectUtil
 * 反射工具类
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 3.20.0
 * <br>
 * Create at 2019/04/11 19:55
 */
@Slf4j
public class ReflectUtil {

    /**
     * 获取父类或接口上泛型对应的Class
     * @param type  泛型原型
     * @return Class[]
     */
    public static Class<?>[] getClassOfParameterizedType(Type type) {
        // 是否带泛型
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] subTypes = parameterizedType.getActualTypeArguments();
            Class<?>[] classes = new Class[subTypes.length];
            for (int i = 0; i < subTypes.length; i++) {
                // 获取泛型类型
                Class<?> clazz = (Class<?>)((ParameterizedType) subTypes[i]).getRawType();
                classes[i] = clazz;
            }
            return classes;
        }
        return null;
    }

    /**
     * 获取方法参数列表
     * @param joinPoint     切点连接点
     * @param ignoreParams  忽略的参数列表
     * @return  方法参数列表
     * @since 3.12.10
     */
    public static Map<String, Object> getMethodParams(JoinPoint joinPoint, List<Class<?>> ignoreParams) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Map<String, Object> params = new HashMap<>();
        // 获取参数名和参数值
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (args !=  null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String argName = parameterNames[i];
                Object argValue = args[i];
                // 参数过滤
                if (ignoreParams != null) {
                    if (ignoreParams.stream().anyMatch(p -> p.isInstance(argValue))) {
                        continue;
                    }
                }
                params.put(argName, argValue);
            }
        }
        if (!params.isEmpty()) {
            return params;
        }
        return null;
    }

    /**
     * 获得方法上的注解
     * @param joinPoint 切点连接点
     * @param annotationClazz 注解类型
     * @param <T> 注解类型
     * @return 注解实现
     */
    public static  <T extends Annotation> T getAnnotation(JoinPoint joinPoint, Class<T> annotationClazz) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        T annotation = method.getAnnotation(annotationClazz);
        if (null != annotation) {
            return annotation;
        }
        // 从方法类上获到注解
        Class<?> targetClass = joinPoint.getTarget().getClass();
        return targetClass.getDeclaredAnnotation(annotationClazz);
    }

    /**
     * 注入参数
     * @param joinPoint 连接点
     * @param obj       注入值
     * @param type      注解
     * @param check     检查参数列表
     * @return  注入后的参数列表
     */
    public static Object[] injectParam(JoinPoint joinPoint, Object obj, Annotation type, boolean check) {
        Object[] args = joinPoint.getArgs();
        int len = args.length;
        for (int i = 0; i < len; i++) {
            if (obj.getClass().isInstance(args[i])) {
                args[i] = obj;
                return args;
            }
        }
        if (check) {
            throw new IllegalArgumentException("You must add " + obj.getClass().getSimpleName() + " parameter on method " +
                    joinPoint.getSignature().getName() + " before use @" + type.annotationType().getSimpleName() + ".");
        }
        return args;
    }


    /**
     * 包装类型智能注入到方法
     * @param target            目标对象
     * @param method            目标方法
     * @param wrapperList       包装对象列表
     * @param wrapperClazz      包装类型
     * @param wrapperBody       获取包装对象的实际业务数据
     * @param wipeWrapperBody   覆写包装对象的实际业务数据
     * @param <T>               包装对象类型
     * @throws IllegalAccessException       非法访问异常
     * @throws InvocationTargetException    方法调用异常
     * @return 返回原有值
     */
    public static <T> Object invokeWithWrapperInject(Object target, Method method, List<T> wrapperList, Class<T> wrapperClazz, Function<T, Object> wrapperBody, BiConsumer<T, Object> wipeWrapperBody) throws IllegalAccessException, InvocationTargetException {
        // 没有参数
        if(method.getParameterTypes().length == 0) {
            return method.invoke(target);
        }

        // ResolvableType是Spring核心包里的泛型解决方案，简化对泛型识别处理（下面注释保留Java API处理方式）
        ResolvableType resolvableType = ResolvableType.forMethodParameter(method, 0);
        // 获取参数类型
        Class<?> parameterClazz = resolvableType.resolve();
        // Map 或 Object
        if (parameterClazz == Map.class || parameterClazz == Object.class) {
            // 去掉实体的包装
            return method.invoke(target, wrapperBody.apply(wrapperList.get(0)));
        }

        // Entity
        if (parameterClazz == wrapperClazz) {
            T entity = wrapperList.get(0);
            ResolvableType[] wrapperGenerics = resolvableType.getGenerics();
            Class<?> elementGenericType = wrapperGenerics[0].resolve();
            // Entity or Entity<Map>
            if (elementGenericType == null || elementGenericType == Map.class) {
                return method.invoke(target, entity);
            }

            // Entity<T>
            Object body = JSONUtil.parse(JSONUtil.serialize(wrapperBody.apply(wrapperList.get(0))), elementGenericType);
            wipeWrapperBody.accept(entity, body);
            return method.invoke(target, entity);
        }

        // List
        if (parameterClazz == List.class) {
            ResolvableType[] wrapperGenerics = resolvableType.getGenerics();
            Class<?> elementGenericType = wrapperGenerics[0].resolve();
            if (elementGenericType == null) {
                // 去掉实体的包装
                return method.invoke(target, wrapperList.stream().map(wrapperBody).collect(Collectors.toList()));
            }

            if (elementGenericType == Map.class) {
                // 去掉实体的包装
                return method.invoke(target, wrapperList.stream().map(wrapperBody).collect(Collectors.toList()));
            }
            if (elementGenericType == wrapperClazz) {
                Class<?> entityGenericType = wrapperGenerics[0].getGeneric(0).resolve();
                if (entityGenericType == null) {
                    return method.invoke(target, wrapperList);
                }
                if (entityGenericType == Map.class) {
                    return method.invoke(target, wrapperList);
                }
                for (T wrapper : wrapperList) {
                    Object body = JSONUtil.parse(JSONUtil.serialize(wrapperBody.apply(wrapper)), entityGenericType);
                    wipeWrapperBody.accept(wrapper, body);
                }
                return method.invoke(target, wrapperList);
            }
            return method.invoke(target, wrapperList.stream().map(wrapper ->
                    JSONUtil.parse(JSONUtil.serialize(wrapperBody.apply(wrapper)), elementGenericType)).collect(Collectors.toList()));
        }

        // 转到业务类型 T
        return method.invoke(target, JSONUtil.parse(JSONUtil.serialize(wrapperBody.apply(wrapperList.get(0))), parameterClazz));
    }


    /**
     * 获取默认类型值
     * @return  默认类型值
     */
    public static Object getTypeDefaultVal(Class<?> type) {
        // 基本数据类型过滤
        if (Long.class == type) {
            return -1L;
        }
        if (long.class == type || int.class == type || short.class == type ||
                float.class == type || double.class == type || Number.class.isAssignableFrom(type)) {
            return -1;
        }
        if (boolean.class == type || Boolean.class.isAssignableFrom(type)) {
            return false;
        }
        if (byte.class == type || Byte.class.isAssignableFrom(type)) {
            return 0;
        }
        if (char.class == type || Character.class.isAssignableFrom(type)) {
            return '\0';
        }
        return null;
    }

    /**
     * 获取方法返回值类型
     * @param joinPoint JoinPoint
     * @return  返回值类型
     */
    public static Class<?> getMethodReturnType(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        // (Class<?>) signature.getReturnType()
        return getMethodReturnType(signature.getMethod());
    }

    /**
     * 获取方法返回值值类型
     * @param method    Method
     * @return  返回值类型
     */
    public static Class<?> getMethodReturnType(Method method) {
        return ResolvableType.forMethodReturnType(method).resolve();
    }

    /**
     * 获取类注解字段值
     * @param annotationClazz   注解 Class
     * @param targetClazz       目标 Class
     * @param recognizer        注解值获取
     * @param resultType        注解值类
     * @return  注解值
     * @param <T>   注解类型
     * @since 3.21.0
     */
    public static <T> T getClazzAnnotatedValue(Class<? extends Annotation> annotationClazz, Class<?> targetClazz, Function<Annotation, T> recognizer, Class<T> resultType) {
        Annotation annotation = AnnotationUtils.findAnnotation(targetClazz, annotationClazz);
        if (annotation == null) {
            return null;
        }
        return recognizer.apply(annotation);
    }

    /**
     * 根据指定的注解获取字段值
     * @param annotationClazz   注解Class
     * @param target            目标对象
     * @param resultType        返回类型
     * @return  字段值
     * @param <T>   返回泛型
     * @since 3.21.0
     */
    @SuppressWarnings("unchecked")
    public static <T> T getAnnotatedFieldValue(Class<? extends Annotation> annotationClazz, Object target, Class<T> resultType) {
        Class<?> clazz = target.getClass();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(annotationClazz)) {
                // 尝试调用 Getter 方法获取字段值
                String getterMethodName = "get" + Character.toUpperCase(field.getName().charAt(0)) + field.getName().substring(1);
                try {
                    try {
                        Method getterMethod = clazz.getMethod(getterMethodName);
                        return (T) getterMethod.invoke(target);
                    } catch (NoSuchMethodException e) {
                        // 如果没有 Getter 方法，直接访问字段
                        field.setAccessible(true);
                        return (T) field.get(target);
                    }
                } catch (IllegalAccessException | InvocationTargetException e) {
                    log.error("Get field value error: {}", e.getMessage(), e);
                }
            }
        }
        return null;
    }

    /**
     * 获取Field和值
     * @param target    目标对象
     * @param fieldPath 属性路径
     * @return  Field和值
     * @since 3.7.1
     */
    public static Pair<Field, Object> getFieldBundlePath(Object target, String fieldPath) {
        if (target == null || StringExtensionsKt.isEmpty(fieldPath)) {
            return null;
        }
        String[] fieldNames = StringUtils.delimitedListToStringArray(fieldPath, ".");
        Field field = null;
        for (String fieldName : fieldNames) {
            field = ReflectionUtils.findField(Objects.requireNonNull(target).getClass(), fieldName);
            if (field == null) {
                return null;
            }
            ReflectionUtils.makeAccessible(field);
            target = ReflectionUtils.getField(field, target);
        }
        return Pair.of(field, target);
    }

    /**
     * 获取属性路径值
     * @param target    目标对象
     * @param fieldPath 属性路径
     * @param <T>       值类型
     * @return  属性值
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeFieldPath(Object target, String fieldPath) {
        Pair<Field, Object> fieldBundle = getFieldBundlePath(target, fieldPath);
        if (fieldBundle == null) {
            return null;
        }
        return (T) fieldBundle.getValue();
    }

    /**
     * 设置属性路径值
     * @param target    目标对象
     * @param fieldPath 属性路径
     * @param value     值
     * @since 3.7.1
     */
    public static void setFieldPath(Object target, String fieldPath, Object value) {
        Pair<Field, Object> fieldBundle = getFieldBundlePath(target, fieldPath);
        if (fieldBundle == null) {
            return;
        }
        ReflectionUtils.setField(fieldBundle.getKey(), fieldBundle.getValue(), value);
    }

    /**
     * 设置对象的值
     * @param target    对象
     * @param props     属性Map
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void setField(Object target, Map<String, Object> props) {
        for (Map.Entry<String, Object> entry : props.entrySet()) {
            Field field = ReflectionUtils.findField(target.getClass(), entry.getKey());
            if (field == null) {
                continue;
            }
            ReflectionUtils.makeAccessible(field);
            // String -> Enum
            if (Enum.class.isAssignableFrom(field.getType()) && entry.getValue() instanceof String) {
                ReflectionUtils.setField(field, target, Enum.valueOf((Class<? extends Enum>) field.getType(), (String) entry.getValue()));
            } else if(Class.class.isAssignableFrom(field.getType()) && entry.getValue() instanceof String) {
                try {
                    ReflectionUtils.setField(field, target, Class.forName(String.valueOf(entry.getValue())));
                } catch (ClassNotFoundException e) {
                    log.error("Set field error with msg: {}", e.getMessage(), e);
                }
            } else if(Long.class.isAssignableFrom(field.getType()) && entry.getValue() instanceof Integer) {
                ReflectionUtils.setField(field, target, Long.valueOf(String.valueOf(entry.getValue())));
            } else if(List.class.isAssignableFrom(field.getType()) && entry.getValue() instanceof Map) {
                ReflectionUtils.setField(field, target, new ArrayList(((LinkedHashMap) entry.getValue()).values()));
            } else {
                ReflectionUtils.setField(field, target, entry.getValue());
            }
        }
    }

    /**
     * 获取方法值
     * @param target        目标对象
     * @param methodName    方法名
     * @param paramTypes    参数类型列表
     * @param args          参数值
     * @param <T>           返回值类型
     * @return  返回值
     */
    @SuppressWarnings("unchecked")
    public static <T> T invokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object... args) {
        Method method = ReflectionUtils.findMethod(target.getClass(), methodName, paramTypes);
        if (method == null) {
            return null;
        }
        ReflectionUtils.makeAccessible(method);
        return (T) ReflectionUtils.invokeMethod(method, target, args);
    }

    /**
     * 根据参数类型动态调用目标对象方法
     * @param target        目标对象
     * @param paramObj      方法参数
     * @param methodName    方法名
     * @since 3.21.0
     */
    @SneakyThrows(InvocationTargetException.class)
    public static void invokeDynamically(Object target, Object paramObj, String methodName) {
        try {
            Method method = target.getClass().getMethod(methodName, paramObj.getClass());
            method.invoke(target, paramObj);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new UnsupportedOperationException(
                    String.format("Aggregate %s doesn't support %s(%s)",
                            target.getClass(), methodName, paramObj.getClass().getSimpleName()), e);
        }
    }

    /**
     * 根据属性类型转换值
     * @param field 属性
     * @param value 字符串
     * @return  目标值
     * @since 3.6.0
     */
    public static Object getTypeValue(Field field, String value) {
        Class<?> type = field.getType();
        if (String.class.equals(type)) {
            return value;
        }
        if (Byte.class.equals(type)) {
            return Byte.valueOf(value);
        }
        if (Boolean.class.equals(type)) {
            if ("0".equals(value)) {
                return false;
            }
            if ("1".equals(value)) {
                return true;
            }
            return Boolean.valueOf(value);
        }
        if (Short.class.equals(type)) {
            return Short.valueOf(value);
        }
        if (Integer.class.equals(type)) {
            return Integer.valueOf(value);
        }
        if (Long.class.equals(type)) {
            return Long.valueOf(value);
        }
        if (Float.class.equals(type)) {
            return Float.valueOf(value);
        }
        if (Double.class.equals(type)) {
            return Double.valueOf(value);
        }
        if (BigDecimal.class.equals(type)) {
            return new BigDecimal(value);
        }
        ResolvableType resolvableType = ResolvableType.forField(field);
        return parseResolvableType(resolvableType, type, value, null);
    }

    /**
     * Get param value from a type.
     * @param methodParameter   MethodParameter
     * @param rawType           param type
     * @param value             request string value
     * @param wrapperBody       get body from wrapper
     * @param wipeWrapperBody   set body with wrapper
     * @return Object
     * @since 3.14.0
     */
    public static Object getParamsTypeValue(MethodParameter methodParameter, Class<?> rawType, String value, Function<Object, Object> wrapperBody, BiConsumer<Object, Object> wipeWrapperBody) {
        ResolvableType resolvableType = ResolvableType.forMethodParameter(methodParameter);
        return parseResolvableType(resolvableType, rawType, value, () -> {
            ResolvableType[] wrapperGenerics = resolvableType.getGenerics();
            Class<?> firstGenericType = wrapperGenerics.length > 0 ? wrapperGenerics[0].resolve() : String.class;
            Object wrapper = JSONUtil.parse(value, rawType);
            Object body = Optional.ofNullable(wrapperBody.apply(wrapper)).orElse("{}");
            Object entity = JSONUtil.parse(JSONUtil.serialize(body), firstGenericType);
            wipeWrapperBody.accept(wrapper, entity);
            return wrapper;
        });
    }

    private static Object parseResolvableType(ResolvableType resolvableType, Class<?> rawType, String value, Supplier<?> additionHandle) {
        if (Map.class.equals(rawType)) {
            ResolvableType[] wrapperGenerics = resolvableType.getGenerics();
            Class<?> firstGenericType = wrapperGenerics.length > 0 ? wrapperGenerics[0].resolve() : String.class;
            Class<?> secondGenericType = wrapperGenerics.length > 1 ? wrapperGenerics[1].resolve() : Object.class;
            return JSONUtil.parseMap(value, firstGenericType, secondGenericType);
        }
        if (List.class.equals(rawType)) {
            ResolvableType[] wrapperGenerics = resolvableType.getGenerics();
            if (wrapperGenerics.length > 0 && wrapperGenerics[0].resolve() == Map.class) {
                return JSONUtil.parseList(value, Map.class);
            }
            return JSONUtil.parseList(value, rawType);
        }

        if (additionHandle != null) {
            Object result = additionHandle.get();
            if (result != null) {
                return result;
            }
        }

        // Entity
        return JSONUtil.parse(value, rawType);
    }

    /**
     * 创建类实例
     * @param clazz 类
     * @param <T>   类的类型
     * @return  实例
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error("create instance error with msg: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 创建非空类实例
     * @param clazz 类
     * @param <T>   类的类型
     * @return  实例
     * @since 3.20.0
     */
    public static <T> T newNonNullInstance(Class<T> clazz) {
        return Objects.requireNonNull(newInstance(clazz));
    }
}
