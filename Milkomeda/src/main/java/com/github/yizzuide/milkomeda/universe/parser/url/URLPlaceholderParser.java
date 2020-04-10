package com.github.yizzuide.milkomeda.universe.parser.url;

import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.PlaceholderResolver;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * URLPlaceholderParser
 * URL占位符解析
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/09 15:07
 */
@Data
public class URLPlaceholderParser {
    // 请求参数解析
    private PlaceholderResolver paramsPlaceholderResolver;
    // 请求头解析
    private static PlaceholderResolver headerPlaceholderResolver = PlaceholderResolver.getResolver("[", "]");
    // 忽略的固定参数
    private static List<String> ignorePlaceHolders = Arrays.asList("uri", "method", "params");

    /**
     * 自定义请求参数解析器
     */
    private URLPlaceholderResolver customURLPlaceholderResolver;

    public static final String KEY_HEAD = "header";
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
        paramsPlaceholderResolver = PlaceholderResolver.getResolver(paramsPrefix, paramsSuffix);
    }

    /**
     * 获取请求头和参数占位符
     * @param tpl   模板
     * @return  Map
     */
    public Map<String, List<String>> grabPlaceHolders(String tpl) {
        Map<String, List<String>> keyMap = new HashMap<>(4);
        keyMap.put(KEY_HEAD, headerPlaceholderResolver.getPlaceHolders(tpl));
        keyMap.put(KEY_PARAMS, paramsPlaceholderResolver.getPlaceHolders(tpl));
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
        Map<String, Object> placeholderResultMap = new HashMap<>();
        placeholderResultMap.put("uri", request.getRequestURI());
        placeholderResultMap.put("method", request.getMethod());
        placeholderResultMap.put("params", StringUtils.isEmpty(params) ? JSONUtil.serialize(request.getParameterMap()) : params);

        // 参数占位替换
        for (String placeHolder : placeHolders.get(KEY_PARAMS)) {
            if (ignorePlaceHolders.contains(placeHolder)) continue;
            Object paramValue = request.getParameter(placeHolder);
            placeholderResultMap.put(placeHolder, paramValue == null ? (this.customURLPlaceholderResolver == null ? "" :
                    String.valueOf(this.customURLPlaceholderResolver.resolver(placeHolder, request))) : paramValue);
        }

        if (CollectionUtils.isEmpty(placeHolders.get(KEY_HEAD))) {
            return paramsPlaceholderResolver.resolveByMap(tpl, placeholderResultMap);
        }

        // 请求头占位替换
        for (String placeHolder : placeHolders.get(KEY_HEAD)) {
            String headerValue = request.getHeader(placeHolder);
            placeholderResultMap.put(placeHolder, headerValue == null ? (this.customURLPlaceholderResolver == null ? "" :
                    String.valueOf(this.customURLPlaceholderResolver.resolver(placeHolder, request))) : headerValue);
        }
        return headerPlaceholderResolver.resolveByMap(paramsPlaceholderResolver.resolveByMap(tpl, placeholderResultMap), placeholderResultMap);
    }
}
