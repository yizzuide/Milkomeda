package com.github.yizzuide.milkomeda.hydrogen.filter;

import lombok.Data;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * FilterLoader
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/01 18:19
 */
@Data
public class FilterLoader {
    private ServletContext servletContext;

    /**
     * 探查过滤器信息
     * @return 过滤器信息列表
     */
    public List<Map<String, String>> inspect() {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, ? extends FilterRegistration> filterRegistrations = servletContext.getFilterRegistrations();
        for (Map.Entry<String, ? extends FilterRegistration> entry : filterRegistrations.entrySet()) {
            Map<String, String> filterInfoMap = new HashMap<>();
            filterInfoMap.put("name",  entry.getKey());
            FilterRegistration filterRegistration = entry.getValue();
            filterInfoMap.put("urlPattern", filterRegistration.getUrlPatternMappings().toString());
            list.add(filterInfoMap);
        }
        return list;
    }
}
