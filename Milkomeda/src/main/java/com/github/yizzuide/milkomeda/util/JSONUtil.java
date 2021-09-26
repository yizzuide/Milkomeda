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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
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
 * @version 3.12.3
 * Create at 2019/04/11 22:07
 */
@Slf4j
public class JSONUtil {
    public static final ObjectMapper mapper = new ObjectMapper();

    static {
        // 排除json字符串中实体类没有的字段
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        TimeZone china = TimeZone.getTimeZone("GMT+08:00");
        mapper.setTimeZone(china);
        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        // 允许特殊字符
        mapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(),true);
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
     * @param data  源数据，Map或List
     * @param clazz 转换的类型
     * @param <T> 返回类型
     * @return 结果类型
     * @throws IOException 转换异常
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T> T toCamel(Object data, TypeReference<T> clazz) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Object result = data;
        boolean isMap = false;
        boolean isList = false;
        if (data instanceof String) {
            isMap = data.toString().matches("^\\s*\\{.+");
            isList = data.toString().matches("^\\s*\\[.+");
            if (isMap) {
                data = JSONUtil.parseMap((String) data, String.class, Object.class);
            } else {
                data = JSONUtil.parseList((String) data, Map.class);
            }
            if (data == null) {
                return null;
            }
        }
        if (isMap || data instanceof Map) {
            result = toCamel((Map) data);
            if (Map.class == TypeUtil.type2Class(clazz)) {
                return (T) result;
            }
        } else if (isList || data instanceof List) {
            List<Map> list = (List) data;
            List<Map> targetList = new ArrayList<>();
            for (Map m : list) {
                targetList.add(toCamel(m));
            }
            result = targetList;
            if (List.class == TypeUtil.type2Class(clazz)) {
                return (T) result;
            }
        }

        String jsonString = mapper.writeValueAsString(result);
        return mapper.readValue(jsonString, clazz);
    }

    /**
     * 将Map的带下划线的key转驼峰key
     * @param data  源Map
     * @return  Map
     */
    @SuppressWarnings("rawtypes")
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
            tmpMap.put(DataTypeConvertUtil.toCamelCase((String) k), v);
        }
        return tmpMap;
    }
}
