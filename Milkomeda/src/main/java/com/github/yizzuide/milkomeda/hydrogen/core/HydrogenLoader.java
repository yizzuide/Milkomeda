package com.github.yizzuide.milkomeda.hydrogen.core;

import java.util.List;
import java.util.Map;

/**
 * HydrogenLoader
 * 加载器
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/03 11:22
 */
public interface HydrogenLoader {

    /**
     * 摘取处理器列表信息
     * @return 处理器列表信息
     */
    List<Map<String, String>> inspect();

    /**
     * 配置刷新，加载处理器
     */
    void refresh();
}
