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

package com.github.yizzuide.milkomeda.universe.parser.url;

import com.github.yizzuide.milkomeda.comet.core.CometRequestWrapper;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.universe.parser.placeholder.PlaceholderExtractor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * URLPlaceholderParser
 * URL占位符解析
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.13.0
 * <br>
 * Create at 2020/04/09 15:07
 */
@Data
public class URLPlaceholderParser {

    // 请求参数解析
    private PlaceholderExtractor placeholderExtractor;
    private String paramsPrefix;
    private String paramsSuffix;

    /**
     * 自定义请求参数解析器
     */
    private URLPlaceholderResolver customURLPlaceholderResolver;

    // 请求数据域
    private static final String headerStartToken = "$header.";
    private static final String cookieStartToken = "$cookie.";
    private static final String paramsStartToken = "$params.";
    private static final String attrStartToken = "$attr.";
    private static final String xStartToken = "$x.";

    public static final String KEY_HEAD = "header";
    public static final String KEY_COOKIE = "cookie";
    public static final String KEY_PARAMS = "params";
    public static final String KEY_ATTR = "attr";
    public static final String KEY_X = "x";

    public URLPlaceholderParser() {
        this("{", "}");
    }

    /**
     * 自定义参数占位符构造
     * @param paramsPrefix  参数占位符前缀
     * @param paramsSuffix  参数占位符后缀
     */
    public URLPlaceholderParser(String paramsPrefix, String paramsSuffix) {
        this.paramsPrefix = paramsPrefix;
        this.paramsSuffix = paramsSuffix;
        placeholderExtractor = PlaceholderExtractor.create(paramsPrefix, paramsSuffix);
    }

    /**
     * 获取占位符
     * @param tpl   模板
     * @return  Map
     */
    public Map<String, List<String>> grabPlaceHolders(String tpl) {
        List<String> placeHolders = placeholderExtractor.getPlaceHolders(tpl);
        Map<String, List<String>> keyMap = new HashMap<>(6);
        keyMap.put(KEY_HEAD, placeHolders.stream().filter(s -> s.startsWith(headerStartToken)).collect(Collectors.toList()));
        keyMap.put(KEY_COOKIE, placeHolders.stream().filter(s -> s.startsWith(cookieStartToken)).collect(Collectors.toList()));
        keyMap.put(KEY_PARAMS, placeHolders.stream().filter(s -> s.startsWith(paramsStartToken)).collect(Collectors.toList()));
        keyMap.put(KEY_ATTR, placeHolders.stream().filter(s -> s.startsWith(attrStartToken)).collect(Collectors.toList()));
        keyMap.put(KEY_X, placeHolders.stream().filter(s -> s.startsWith(xStartToken)).collect(Collectors.toList()));
        return keyMap;
    }

    /**
     * 根据模板解析
     * @param tpl           模板
     * @param request       请求对象
     * @param params        请求数据
     * @param resp          响应数据
     * @param placeHolders  占位列表
     * @return  解析后的结果
     */
    public String parse(String tpl, HttpServletRequest request, String params, String resp, Map<String, List<String>> placeHolders) {
        if (params == null) {
            params = CometRequestWrapper.resolveRequestParams(request, true);
        }
        Map<String, Object> placeholderResultMap = new HashMap<>();
        placeholderResultMap.put("uri", request.getRequestURI());
        placeholderResultMap.put("method", request.getMethod());
        placeholderResultMap.put(KEY_PARAMS, params);
        if (resp != null) {
            placeholderResultMap.put("resp", resp);
        }

        // Custom field
        replaceHolders(placeHolders.get(KEY_X), xStartToken, placeholderResultMap, actualPlaceHolder -> {
            if (this.customURLPlaceholderResolver == null) {
                return null;
            }
            return String.valueOf(this.customURLPlaceholderResolver.resolver(actualPlaceHolder, request));
        });

        // Request params
        Map<String, Object> paramsMap = null;
        if (StringUtils.isNotEmpty(params)) {
            paramsMap = JSONUtil.parseMap(params, String.class, Object.class);
        }
        final Map<String, Object> finalParamsMap = paramsMap;
        replaceHolders(placeHolders.get(KEY_PARAMS), paramsStartToken, placeholderResultMap, actualPlaceHolder ->
                finalParamsMap == null ? request.getParameter(actualPlaceHolder) :
                DataTypeConvertUtil.extractPath(actualPlaceHolder, finalParamsMap, null));

        // Request attr
        replaceHolders(placeHolders.get(KEY_ATTR), attrStartToken, placeholderResultMap, actualPlaceHolder -> {
            String attrValue;
            if (actualPlaceHolder.contains(".")) { // 复合对象
                String attrKey = actualPlaceHolder.substring(0, actualPlaceHolder.indexOf("."));
                String subKeyPath = actualPlaceHolder.substring(actualPlaceHolder.indexOf(".") + 1);
                attrValue = DataTypeConvertUtil.extractPath(subKeyPath, request.getAttribute(attrKey), null);
            } else { // 普通值
                Object val = request.getAttribute(actualPlaceHolder);
                if (val == null) {
                    return null;
                }
                attrValue = val.toString();
            }
            return attrValue;
        });

        // Request Header
        replaceHolders(placeHolders.get(KEY_HEAD), headerStartToken, placeholderResultMap, request::getHeader);

        // Request cookie
        replaceHolders(placeHolders.get(KEY_COOKIE), cookieStartToken, placeholderResultMap, actualPlaceHolder ->
                Stream.of(request.getCookies()).filter(cookie -> cookie.getName().equals(actualPlaceHolder))
                        .findFirst().orElse(new Cookie("_", "")).getValue());
        return placeholderExtractor.replacePlaceholders(tpl, placeholderResultMap);
    }

    /**
     * 占位符插值
     * @param placeHolders          占位符列表
     * @param splitToken            类型分隔
     * @param placeholderResultMap  占位结果集
     * @param resolve               根据占位符获取值
     */
    private void replaceHolders(List<String> placeHolders, String splitToken, Map<String, Object> placeholderResultMap, Function<String, String> resolve) {
        for (String placeHolder : placeHolders) {
            String actualPlaceHolder = placeHolder.substring(splitToken.length());
            String value = resolve.apply(actualPlaceHolder);
            placeholderResultMap.put(placeHolder, value);
        }
    }
}
