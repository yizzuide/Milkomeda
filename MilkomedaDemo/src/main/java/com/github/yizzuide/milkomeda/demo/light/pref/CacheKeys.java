package com.github.yizzuide.milkomeda.demo.light.pref;

/**
 * CacheKeys
 *
 * @author yizzuide
 * <br>
 * Create at 2019/12/24 10:35
 */
public class CacheKeys {
    public static final String GLOBAL_KEY = "plat_";

    private static CacheKey build(String key, int expire) {
        return new CacheKey(GLOBAL_KEY + key, expire <= 0 ? CacheKey.MINUTES30 : expire);
    }

    public static final CacheKey ORDER = build("order", 0);
}
