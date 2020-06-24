package com.github.yizzuide.milkomeda.util;


import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DataTypeConvertUtil
 * 数据类型转换工具类
 *
 * @author yizzuide
 * @since 1.13.0
 * @version 3.8.0
 * Create at 2019/09/21 17:23
 */
public class DataTypeConvertUtil {

    private static final Pattern linePattern = Pattern.compile("_(\\w)");

    /**
     * 下划线转驼峰
     * @param str 源字符串
     * @return 驼峰风格字符串
     */
    public static String toCamelCase(String str) {
        str = str.toLowerCase();
        Matcher matcher = linePattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, matcher.group(1).toUpperCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Object 转 String
     * @param obj   Object
     * @return  String
     */
    public static String toString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.toString();
    }

    /**
     * Object 转 Long
     * @param obj Object
     * @return Integer
     */
    public static Long toLong(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof Double || obj instanceof Float) {
            return Long.valueOf(StringUtils.substringBefore(obj.toString(), "."));
        }
        if (obj instanceof Number) {
            return Long.valueOf(obj.toString());
        }
        if (obj instanceof String) {
            return Long.valueOf(obj.toString());
        } else {
            return 0L;
        }
    }

    /**
     * Object 转 int
     * @param obj Object
     * @return int
     */
    public static Integer toInt(Object obj) {
        return toLong(obj).intValue();
    }

    /**
     * Long 转 Integer
     * @param num Long
     * @return Integer
     */
    public static Integer intVal(Long num) {
        return Math.toIntExact(num);
    }

    /**
     * 将二进制转换成16进制
     *
     * @param buf 字节数组
     * @return 16进制字符串
     */
    public static String byte2HexStr(byte[] buf) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buf) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            sb.append(hex.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将16进制转换为二进制
     *
     * @param hexStr 16进制字符串
     * @return 字节数组
     */
    public static byte[] hexStr2Byte(String hexStr) {
        if (hexStr.length() < 1)
            return null;
        byte[] result = new byte[hexStr.length() / 2];
        for (int i = 0; i < hexStr.length() / 2; i++) {
            int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2), 16);
            result[i] = (byte) (high * 16 + low);
        }
        return result;
    }

    /**
     * 对象转Map
     * @param object 源对象
     * @return  Map
     */
    public static Map<String, Object> beanToMap(Object object) {
        Map<String, Object> map;
        try {
            map = new HashMap<>();

            BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();
                if (key.compareToIgnoreCase("class") == 0) {
                    continue;
                }
                Method getter = property.getReadMethod();
                Object value = getter != null ? getter.invoke(object) : null;
                map.put(key, value);
            }
            // 可能会把自己的 class 和 hashcode 编进去，直接去掉
            map.remove("class");
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
        Set<String> set = map.keySet();
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            String key = it.next();
            if (map.get(key) == null || map.get(key) == "") {
                map.remove(key);
                set = map.keySet();
                it = set.iterator();
            }
        }
        return map;
    }

    /**
     * 返回排序的Map
     * @param map               源Map
     * @param deleteNullValue   是否去空
     * @return 排序后Map
     */
    public static Map<String, Object> sortMap(Map<String, Object> map, boolean deleteNullValue) {
        if (deleteNullValue) {
           clearEmptyValue(map);
        }
        return new TreeMap<>(map);
    }

    /**
     * Map去空
     * @param map   源Map
     */
    public static void clearEmptyValue(Map<String, Object> map) {
        List<String> filterList = new ArrayList<>();
        for (String key : map.keySet()) {
            Object val = map.get(key);
            if (val == null || (val instanceof String && StringUtils.isEmpty((CharSequence) val))) {
                filterList.add(key);
            }
        }
        for (String key : filterList) {
            map.remove(key);
        }
    }

    /**
     * map转FORM_URLENCODED
     * @param map               原Map
     * @param deleteNullValue   是否去空
     * @return 表单字段
     */
    @SuppressWarnings("rawtypes")
    public static String map2FormData(Map<String, Object> map, boolean deleteNullValue) {
        map = sortMap(map, deleteNullValue);
        int count = map.size();
        StringBuilder builder = new StringBuilder(32);
        int index = 0;
        for (String key : map.keySet()) {
            Object obj = map.get(key);
            StringBuilder value = new StringBuilder();
            if (obj instanceof List) {
                List list = (List) obj;
                for (int i = 0; i < list.size(); i++) {
                    value.append(list.get(i).toString());
                    if (i < list.size() - 1) {
                        builder.append(",");
                    }
                }
            } else {
                value.append(obj.toString());
            }
            if (StringUtils.isNotEmpty(value.toString())) {
                builder.append(key).append('=').append(value.toString());
                if (index < count - 1) {
                    builder.append("&");
                }
            }
            index++;
        }
        return builder.toString();
    }

    /**
     * 从源对象采集key的值
     * @param key       采集key
     * @param source    源Map
     * @return 采集到的字符串
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String extractValue(String key, Object source) {
        if (source instanceof Map) {
            return extractValue(key, (Map) source, null);
        }
        return extractValue(key, beanToMap(source), null);
    }

    /**
     * 从源Map采集key的值
     * @param key       采集key
     * @param source    源Map
     * @return 采集到的字符串
     */
    public static String extractValue(String key, Map<String, Object> source) {
        return extractValue(key, source, null);
    }

    /**
     * 从源Map采集key的值
     * @param key           采集key
     * @param source        源Map
     * @param defaultValue  默认值
     * @return 采集到的字符串
     * @since 3.0.3
     */
    public static String extractValue(String key, Map<String, Object> source, String defaultValue) {
        if (key == null || source == null) {
            return defaultValue;
        }
        Object value = source.get(key);
        if (value == null) return defaultValue;
        return value.toString();
    }

    /**
     * 从源对象采集key路径的值
     * @param keyPath       采集key
     * @param source        源Map
     * @param defaultValue  默认值
     * @return 采集到的字符串
     * @since 3.0.3
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static String extractPath(String keyPath, Object source, String defaultValue) {
        if (source == null) {
            return defaultValue;
        }
        if (source instanceof Map) {
            return extractPath(keyPath, (Map) source, defaultValue);
        }
        if (source instanceof String) {
            return extractPath(keyPath, JSONUtil.parseMap(source.toString(), String.class, Object.class), defaultValue);
        }
        return extractPath(keyPath, beanToMap(source), defaultValue);
    }

    /**
     * 从源Map采集key路径的值
     * @param keyPath       采集key
     * @param source        源Map
     * @param defaultValue  默认值
     * @return 采集到的字符串
     * @since 3.0.3
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static String extractPath(String keyPath, Map<String, Object> source, String defaultValue) {
        if (source == null) {
            return defaultValue;
        }
        if (StringUtils.isEmpty(keyPath)) {
            return defaultValue;
        }
        if (!keyPath.contains(".")) {
            return extractValue(keyPath, source, defaultValue);
        }
        String[] keys = org.springframework.util.StringUtils.delimitedListToStringArray(keyPath, ".");
        Map<String, Object> nodeMap = new HashMap<>(source);
        for (String key : keys) {
            // Map类型路径
            if (key.indexOf('[') == -1) {
                Object value = nodeMap.get(key);
                Object nextNode = findNextNode(value, defaultValue);
                // defaultValue为null时，nextNode为空
                if (nextNode == null || nextNode.equals(defaultValue)) {
                    return defaultValue;
                }
                if (isSugarType(nextNode)) return nextNode.toString();
                nodeMap = (Map<String, Object>) nextNode;
                continue;
            }

            // List类型路径
            int startBracket = key.indexOf('[');
            int index = Integer.parseInt(key.substring(startBracket + 1, key.indexOf(']')));
            key = key.substring(0, startBracket);
            Object node = nodeMap.get(key);
            if (node == null) {
                return defaultValue;
            }
            // list string -> list
            if (node instanceof String && ((String) node).indexOf('[') == 0) {
                node = JSONUtil.parseList(node.toString(), Map.class);
            }
            if (!(node instanceof List)) {
                return defaultValue;
            }
            if (CollectionUtils.isEmpty(((List) node))) {
                return defaultValue;
            }
            Object value = ((List) node).get(index);
            Object nextNode = findNextNode(value, defaultValue);
            // defaultValue为null时，nextNode为空
            if (nextNode == null || nextNode.equals(defaultValue)) {
                return defaultValue;
            }
            if (isSugarType(nextNode)) return nextNode.toString();
            nodeMap = (Map<String, Object>) nextNode;
        }
        // 未找到路径，返回默认值
        return defaultValue;
    }

    /**
     * 是否是简单值类型
     * @param obj   对象
     * @return  false为容器类型
     * @since 3.0.3
     */
    public static boolean isSugarType(Object obj) {
        return !(obj instanceof List) && !(obj instanceof Map);
    }


    private static Object findNextNode(Object node, String defaultValue) {
        if (node == null) {
            return defaultValue;
        }
        Map<String, Object> nodeMap;
        if (isSugarType(node)) {
            String strValue = node.toString();
            // map string -> map
            if (strValue.startsWith("{") && strValue.endsWith("}"))  {
                nodeMap = JSONUtil.parseMap(strValue, String.class, Object.class);
                if (nodeMap == null) {
                    return defaultValue;
                }
                return nodeMap;
            }
            return strValue;
        }
        return node;
    }
}