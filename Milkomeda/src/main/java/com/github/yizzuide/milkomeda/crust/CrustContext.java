package com.github.yizzuide.milkomeda.crust;

import org.springframework.lang.NonNull;

/**
 * CrustContext
 * Crust上下文，使用这个类直接访问内部提供的API
 *
 * @author yizzuide
 * @since 1.14.0
 * @version 2.0.4
 * Create at 2019/11/11 17:53
 */
public class CrustContext {

    private static Crust INSTANCE;

    private CrustContext() {}

    static void set(Crust crust) {
        INSTANCE = crust;
    }

    @NonNull
    public static Crust get() {
        return INSTANCE;
    }

    /**
     * 获取用户信息（支持多次调用，而不会重新创建多份对象）
     *
     * @param entityClass   用户实体类型
     * @param <T>   实体类型
     * @return  CrustUserInfo
     */
    @NonNull
    public static <T> CrustUserInfo<T> getUserInfo(@NonNull Class<T> entityClass) { return get().getUserInfo(entityClass); }

    /**
     * 使登录信息失效，清空当前用户的缓存
     */
    public static void invalidate() {
        get().invalidate();
    }
}
