package com.github.yizzuide.milkomeda.jupiter;

import java.util.HashMap;
import java.util.Map;

/**
 * JupiterCompilerPool
 * 解析器共享池
 *
 * @author yizzuide
 * @since 3.5.0
 * Create at 2020/05/20 00:59
 */
public class JupiterCompilerPool {
    private static final Map<String, JupiterExpressCompiler> sharedCompiler = new HashMap<>();

    static void put(String key, JupiterExpressCompiler compiler) {
        sharedCompiler.put(key, compiler);
    }

    static JupiterExpressCompiler get(String key) {
        return sharedCompiler.get(key);
    }
}
