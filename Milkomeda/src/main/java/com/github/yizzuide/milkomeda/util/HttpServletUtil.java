package com.github.yizzuide.milkomeda.util;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * HttpServletUtil
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 3.0.0
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
        Map<String, String[]> names = request.getParameterMap();
        Map<String, Object> inputs = new HashMap<>();
        for (Map.Entry<String, String[]> paramEntry : names.entrySet()) {
            String[] value = paramEntry.getValue();
            if (value == null) {
                inputs.put(paramEntry.getKey(), "");
            } else if (value.length == 1) {
                inputs.put(paramEntry.getKey(), value[0]);
            } else {
                inputs.put(paramEntry.getKey(), value);
            }
        }
        return JSONUtil.serialize(inputs);
    }
}
