package com.github.yizzuide.milkomeda.ice;

/**
 * IceHolder
 * 一个管理Ice实例的类
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/09 14:03
 */
public class IceHolder {

    private static Ice ice;

    static void setIce(Ice ice) {
        IceHolder.ice = ice;
    }

    /**
     * 获取Ice实现
     * @return  Ice
     */
    public static Ice getIce() {
        return ice;
    }
}
