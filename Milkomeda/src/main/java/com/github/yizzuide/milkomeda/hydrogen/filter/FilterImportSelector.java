package com.github.yizzuide.milkomeda.hydrogen.filter;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

/**
 * FilterImportSelector
 * 容器配置选择器
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/03 00:52
 */
public class FilterImportSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        boolean tomcatPresent = ClassUtils.isPresent("org.apache.catalina.core.StandardContext", getClass().getClassLoader());
        return tomcatPresent ? new String[] {"com.github.yizzuide.milkomeda.hydrogen.filter.TomcatFilterConfig"}
                : new String[] {};
    }
}
