package com.github.yizzuide.milkomeda.demo.light.pref;

import lombok.Data;

/**
 * CacheKey
 *
 * @author yizzuide
 * <br>
 * Create at 2019/12/24 10:36
 */
@Data
public class CacheKey {

    /**
     * 超时时间
     */
    private Integer	expire;
    /**
     * 缓存 key
     */
    private String key;

    /**
     * 1分钟
     */
    public static final int	MINUTES1	= 60;

    /**
     * 30分钟
     */
    public static final int	MINUTES30	= 30 * MINUTES1;

    public CacheKey(String key, Integer expire) {
        this.expire = expire;
        this.key = key;
    }
}
