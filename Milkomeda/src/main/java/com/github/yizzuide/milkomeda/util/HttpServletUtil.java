package com.github.yizzuide.milkomeda.util;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpServletUtil
 *
 * @author yizzuide
 * @since 0.2.0
 * Create at 2019/04/11 20:10
 */
public class HttpServletUtil {
    /**
     * 获取请求参数 转换成 json字符串
     *
     * @param request HttpServletRequest
     * @return json字符串
     */
    public static String getRequestData(HttpServletRequest request) {
        Enumeration<String> names = request.getParameterNames();
        Map<String, String> inputs = new HashMap<>();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = request.getParameter(name);
            if (value.length() > 100) {
                String vString = value.substring(0, 50) + value.substring(value.length() - 50);
                inputs.put(name, vString);
            } else {
                inputs.put(name, value);
            }
        }
        return JSONUtil.serialize(inputs);
    }

    /**
     * 将对象转换成json字符串
     *
     * @param object 返回的数据
     * @return json字符串
     */
    public static String getResponseData(Object object) {
        if (object != null) {
            if (object instanceof Map) {
                return JSONUtil.serialize(object);
            }
            Map<String, Object> map = new HashMap<>();
            // 得到类对象
            Class clazz = object.getClass();
            /* 得到类中的所有属性集合 */
            Field[] fs = clazz.getDeclaredFields();
            for (Field f : fs) {
                f.setAccessible(true); // 设置些属性是可以访问的
                Object val;
                try {
                    val = f.get(object);
                    // 得到此属性的值
                    map.put(f.getName(), val); // 设置键值
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            return JSONUtil.serialize(map);
        }
        return null;
    }
}
