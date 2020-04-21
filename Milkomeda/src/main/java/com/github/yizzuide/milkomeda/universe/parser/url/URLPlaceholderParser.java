package com.github.yizzuide.milkomeda.universe.parser.url;

import com.github.yizzuide.milkomeda.comet.core.CometRequestWrapper;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.PlaceholderResolver;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * URLPlaceholderParser
 * URL占位符解析
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.1.0
 * Create at 2020/04/09 15:07
 */
@Data
public class URLPlaceholderParser {

    // 请求参数解析
    private PlaceholderResolver placeholderResolver;
    private String paramsPrefix;
    private String paramsSuffix;

    /**
     * 自定义请求参数解析器
     */
    private URLPlaceholderResolver customURLPlaceholderResolver;

    // 固定参数
    private static List<String> ignorePlaceHolders = Arrays.asList("uri", "method", "params");

    // 请求数据域
    private static final String headerStartToken = "$header.";
    private static final String cookieStartToken = "$cookie.";
    private static final String paramsStartToken = "$params.";

    public static final String KEY_HEAD = "header";
    public static final String KEY_COOKIE = "cookie";
    public static final String KEY_PARAMS = "params";

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
        placeholderResolver = PlaceholderResolver.getResolver(paramsPrefix, paramsSuffix);
    }

    /**
     * 获取请求头和参数占位符
     * @param tpl   模板
     * @return  Map
     */
    public Map<String, List<String>> grabPlaceHolders(String tpl) {
        List<String> placeHolders = placeholderResolver.getPlaceHolders(tpl);
        Map<String, List<String>> keyMap = new HashMap<>(6);
        keyMap.put(KEY_HEAD, placeHolders.stream().filter(s -> s.startsWith(headerStartToken)).collect(Collectors.toList()));
        keyMap.put(KEY_COOKIE, placeHolders.stream().filter(s -> s.startsWith(cookieStartToken)).collect(Collectors.toList()));
        keyMap.put(KEY_PARAMS, placeHolders.stream().filter(s -> s.startsWith(paramsStartToken)).collect(Collectors.toList()));
        return keyMap;
    }

    /**
     * 根据模板解析
     * @param tpl           模板
     * @param request       请求对象
     * @param params        请求参数
     * @param placeHolders  占位列表
     * @return  解析后的结果
     */
    public String parse(String tpl, HttpServletRequest request, String params, Map<String, List<String>> placeHolders) {
        if (params == null) {
            params = CometRequestWrapper.resolveRequestParams(request, true);
        }
        Map<String, Object> placeholderResultMap = new HashMap<>();
        placeholderResultMap.put("uri", request.getRequestURI());
        placeholderResultMap.put("method", request.getMethod());
        placeholderResultMap.put(KEY_PARAMS, params);

        Map<String, Object> paramsMap = null;
        if (StringUtils.isNotEmpty(params)) {
            paramsMap = JSONUtil.parseMap(params, String.class, Object.class);
        }

        // 参数占位替换
        for (String placeHolder : placeHolders.get(KEY_PARAMS)) {
            if (ignorePlaceHolders.contains(placeHolder)) continue;
            String actualPlaceHolder = placeHolder.substring(paramsStartToken.length());
            Object paramValue = paramsMap == null ? request.getParameter(actualPlaceHolder) :
                    DataTypeConvertUtil.extractPath(actualPlaceHolder, paramsMap, "");
            placeholderResultMap.put(placeHolder, paramValue == null ? (this.customURLPlaceholderResolver == null ? "" :
                    String.valueOf(this.customURLPlaceholderResolver.resolver(actualPlaceHolder, request))) : paramValue);
        }

        // 请求头占位替换
        for (String placeHolder : placeHolders.get(KEY_HEAD)) {
            String actualPlaceHolder = placeHolder.substring(headerStartToken.length());
            String headerValue = request.getHeader(actualPlaceHolder);
            placeholderResultMap.put(placeHolder, headerValue == null ? (this.customURLPlaceholderResolver == null ? "" :
                    String.valueOf(this.customURLPlaceholderResolver.resolver(actualPlaceHolder, request))) : headerValue);
        }

        // cookie占位替换
        for (String placeHolder : placeHolders.get(KEY_COOKIE)) {
            String actualPlaceHolder = placeHolder.substring(cookieStartToken.length());
            String cookieValue = null;
            Cookie[] cookies = request.getCookies();
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(actualPlaceHolder)) {
                    cookieValue = cookie.getValue();
                    break;
                }
            }
            placeholderResultMap.put(placeHolder, cookieValue == null ? (this.customURLPlaceholderResolver == null ? "" :
                    String.valueOf(this.customURLPlaceholderResolver.resolver(actualPlaceHolder, request))) : cookieValue);
        }
        return placeholderResolver.resolveByMap(tpl, placeholderResultMap);
    }
}
