package com.github.yizzuide.milkomeda.hydrogen.filter;

import com.github.yizzuide.milkomeda.hydrogen.core.AbstractHydrogenLoader;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AbstractFilterLoader
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/03 01:01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractFilterLoader extends AbstractHydrogenLoader implements FilterLoader {
    /**
     * Servlet上下文
     */
    private ServletContext servletContext;


    public List<Map<String, String>> inspect() {
        List<Map<String, String>> list = new ArrayList<>();
        Map<String, ? extends FilterRegistration> filterRegistrations = servletContext.getFilterRegistrations();
        for (Map.Entry<String, ? extends FilterRegistration> entry : filterRegistrations.entrySet()) {
            Map<String, String> filterInfoMap = new HashMap<>();
            filterInfoMap.put("name",  entry.getKey());
            FilterRegistration filterRegistration = entry.getValue();
            filterInfoMap.put("class", filterRegistration.getClassName());
            filterInfoMap.put("urlPattern", filterRegistration.getUrlPatternMappings().toString());
            list.add(filterInfoMap);
        }
        return list;
    }
}
