package com.github.yizzuide.milkomeda.util;

import com.fasterxml.jackson.databind.JavaType;
import com.github.yizzuide.milkomeda.universe.el.ELContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ReflectUtil
 * 反射工具类
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 2.8.0
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
            ParameterizedType pt = (ParameterizedType)type;
            Type[] subTypes = pt.getActualTypeArguments();
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
     * 获得方法上的注解
     * @param joinPoint 切点连接点
     * @param annotationClass 注解类型
     * @param <T> 注解类型
     * @return 注解实现
     */
    public static <T extends Annotation> T getAnnotation(JoinPoint joinPoint, Class<T> annotationClass) {
        // 方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 获得方法上注解类与相关信息
        return methodSignature.getMethod().getAnnotation(annotationClass);
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
     * 根据EL表达式或内置头表达式抽取值
     * @param joinPoint 切面连接点
     * @param express   表达式
     * @return 解析的值
     */
    public static String extractValue(JoinPoint joinPoint, String express) {
        // 解析Http请求头
        if (express.startsWith(":")) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert attributes != null;
            String headerName = express.substring(1);
            String value = attributes.getRequest().getHeader(headerName);
            if (StringUtils.isEmpty(headerName) || StringUtils.isEmpty(value)) {
                throw new IllegalArgumentException("Can't find " + headerName + " from HTTP header.");
            }
            return value;
        }

        // 解析EL表达式
        if (express.startsWith("'") || express.startsWith("@") || express.startsWith("#") || express.startsWith("T(") || express.startsWith("args[")) {
            return ELContext.getValue(joinPoint, express);
        }
        return express;
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
     */
    public static <T> void invokeWithWrapperInject(Object target, Method method, List<T> wrapperList, Class<T> wrapperClazz, Function<T, Object> wrapperBody, BiConsumer<T, Object> wipeWrapperBody) throws IllegalAccessException, InvocationTargetException {
        // 没有参数
        if(method.getParameterTypes().length == 0) {
            method.invoke(target);
            return;
        }

        // 获取参数类型
        Class<?> parameterClazz = method.getParameterTypes()[0];
        // Map
        if (parameterClazz == Map.class) {
            // 去掉实体的包装
            method.invoke(target, wrapperBody.apply(wrapperList.get(0)));
            return;
        }

        // List
        if (parameterClazz == List.class) {
            Type[] genericParameterTypes = method.getGenericParameterTypes();
            if (!(genericParameterTypes[0] instanceof ParameterizedType)) {
                // 去掉实体的包装
                method.invoke(target, wrapperList.stream().map(wrapperBody).collect(Collectors.toList()));
                return;
            }
            // List<?>
            ParameterizedType parameterizedType = (ParameterizedType) genericParameterTypes[0];
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Type actualTypeArgument = actualTypeArguments[0];
            // List<Map>
            if (TypeUtil.type2Class(actualTypeArgument) == Map.class) {
                // 去掉实体的包装
                method.invoke(target, wrapperList.stream().map(wrapperBody).collect(Collectors.toList()));
                return;
            }
            // List<Entity>
            if (TypeUtil.type2Class(actualTypeArgument) == wrapperClazz) {
                // List<Entity>
                if (!(actualTypeArgument instanceof ParameterizedType)) {
                    method.invoke(target, wrapperList);
                    return;
                }
                // List<Entity<Map>>
                Type[] subActualTypeArguments = ((ParameterizedType) actualTypeArgument).getActualTypeArguments();
                if (TypeUtil.type2Class(subActualTypeArguments[0]) == Map.class) {
                    method.invoke(target, wrapperList);
                    return;
                }
                // List<Entity<T>>
                JavaType javaType = TypeUtil.type2JavaType(subActualTypeArguments[0]);
                for (T wrapper : wrapperList) {
                    Object body = JSONUtil.nativeRead(JSONUtil.serialize(wrapperBody.apply(wrapper)), javaType);
                    wipeWrapperBody.accept(wrapper, body);
                }
                method.invoke(target, wrapperList);
                return;
            }

            // List<T>
            method.invoke(target, wrapperList.stream()
                    .map(wrapper -> JSONUtil.nativeRead(JSONUtil.serialize(wrapperBody.apply(wrapper)),
                            TypeUtil.type2JavaType(actualTypeArgument))).collect(Collectors.toList()));
            return;
        }

        // 转到业务类型 T
        method.invoke(target, JSONUtil.nativeRead(JSONUtil.serialize(wrapperBody.apply(wrapperList.get(0))), TypeUtil.class2TypeRef(parameterClazz)));
    }


    /**
     * 获取方法返回类型，并返回默认类型值
     * @param joinPoint JoinPoint
     * @return  默认类型值
     */
    public static Object getMethodDefaultReturnVal(JoinPoint joinPoint) {
        Class<?> retType = getMethodReturnType(joinPoint);
        // 基本数据类型过滤
        if (Long.class == retType) {
            return -1L;
        }
        if (long.class == retType || int.class == retType || short.class == retType ||
                float.class == retType || double.class == retType || Number.class.isAssignableFrom(retType)) {
            return -1;
        }
        if (boolean.class == retType || Boolean.class.isAssignableFrom(retType)) {
            return false;
        }
        if (byte.class == retType || Byte.class.isAssignableFrom(retType)) {
            return 0;
        }
        if (char.class == retType || Character.class.isAssignableFrom(retType)) {
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
        return (Class<?>) signature.getReturnType();
    }
}
