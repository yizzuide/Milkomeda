package com.github.yizzuide.milkomeda.crust;

/**
 * CrustContext
 * Crust上下文，使用这个类直接访问内部提供的API
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 17:53
 */
public class CrustContext {

    private static Crust INSTANCE;

    public static void set(Crust crust) {
        INSTANCE = crust;
    }

    public static Crust get() {
        return INSTANCE;
    }
}
