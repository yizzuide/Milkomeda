package com.github.yizzuide.milkomeda.jupiter;

/**
 * JupiterRuleEngine
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/19 14:35
 */
public interface JupiterRuleEngine {

    String DEFAULT_SYNTAX = "el";

    /**
     * 检查规则
     * @return 是否通过，false拦截
     */
    boolean inspect();
}
