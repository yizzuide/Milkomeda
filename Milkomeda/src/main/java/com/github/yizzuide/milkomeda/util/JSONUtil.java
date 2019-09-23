package com.github.yizzuide.milkomeda.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * JSONUtil
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 1.13.1
 * Create at 2019/04/11 22:07
 */
@Slf4j
public class JSONUtil {
    public static final ObjectMapper mapper = new ObjectMapper();

    static {
        // 排除json字符串中实体类没有的字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//        mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        TimeZone china = TimeZone.getTimeZone("GMT+08:00");
        mapper.setTimeZone(china);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    public static String serialize(Object obj) {
        if (obj == null) {
            return null;
        }
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("serialize error：{}", obj, e);
            return null;
        }
    }

    public static <T> T parse(String json, Class<T> tClass) {
        try {
            return mapper.readValue(json, tClass);
        } catch (IOException e) {
            log.error("parse error：{}", json, e);
            return null;
        }
    }

    public static <E> List<E> parseList(String json, Class<E> eClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, eClass));
        } catch (IOException e) {
            log.error("parse error：{}", json, e);
            return null;
        }
    }

    public static <K, V> Map<K, V> parseMap(String json, Class<K> kClass, Class<V> vClass) {
        try {
            return mapper.readValue(json, mapper.getTypeFactory().constructMapType(Map.class, kClass, vClass));
        } catch (IOException e) {
            log.error("parse error：{}", json, e);
            return null;
        }
    }

    public static <T> T nativeRead(String json, TypeReference<T> type) {
        try {
            return mapper.readValue(json, type);
        } catch (IOException e) {
            log.error("parse error：{}", json, e);
            return null;
        }
    }

    public static <T> T nativeRead(String json, JavaType javaType) {
        try {
            return mapper.readValue(json, javaType);
        } catch (IOException e) {
            log.error("parse error：{}", json, e);
            return null;
        }
    }

    /**
     * 将下划线转换为驼峰的形式，例如：user_name 转 userName
     *
     * @param data  Map
     * @param clazz 转换的类型
     * @param <T> 返回类型
     * @return 结果类型
     * @throws IOException 转换异常
     */
    @SuppressWarnings("unchecked")
    public static <T> T toCamel(Map data, TypeReference<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> targetMap = toCamel(data);
        if (Map.class == TypeUtil.type2Class(clazz)) {
            return (T) targetMap;
        }
        String jsonString = mapper.writeValueAsString(targetMap);
        return mapper.readValue(jsonString, clazz);
    }

    /**
     * 将Map的带下划线的key转驼峰key
     * @param data  源Map
     * @return  Map
     */
    public static Map<String, Object> toCamel(Map data) {
        Map<String, Object> tmpMap = new HashMap<>();
        for (Object k : data.keySet()) {
            Object v = data.get(k);
            if (v instanceof Map) {
                v = toCamel((Map) v);
            } else if (v instanceof List) {
                List list = (List) v;
                List<Map> mapList = new ArrayList<>();
                for (Object map : list) {
                    mapList.add(toCamel((Map) map));
                }
                v = mapList;
            }
            tmpMap.put(DataTypeConvertUtil.lineToSnakeStyle((String) k), v);
        }
        return tmpMap;
    }
}
