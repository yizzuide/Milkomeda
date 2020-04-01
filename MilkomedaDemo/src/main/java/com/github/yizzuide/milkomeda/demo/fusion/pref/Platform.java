package com.github.yizzuide.milkomeda.demo.fusion.pref;

/**
 * Platform
 *
 * @author yizzuide
 * Create at 2020/01/02 14:42
 */
public class Platform {
    public static final String EL_CHECK_ACTIVE = "T(com.github.yizzuide.milkomeda.demo.fusion.pref.Platform).checkActive()";
    public static final String EL_IS_TEST = "T(com.github.yizzuide.milkomeda.demo.fusion.pref.Platform).isTest()";
    /**
     * 检测推送开关
     */
    public static boolean checkActive() {
        return false;
    }

    /**
     * 测试环境
     */
    public static boolean isTest()  {
        return true;
    }
}
