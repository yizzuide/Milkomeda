package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;

/**
 * IceHolder
 * 一个管理Ice实例的类
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.0.7
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

    /**
     * 修改实例名（用于多产品）
     * @param instanceName  实例名
     * @since 3.0.7
     */
    public static void setInstanceName(String instanceName) {
        IceInstanceChangeEvent event = new IceInstanceChangeEvent(instanceName);
        ApplicationContextHolder.get().publishEvent(event);
    }
}
