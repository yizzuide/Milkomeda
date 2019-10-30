package com.github.yizzuide.milkomeda.util;

/**
 * RecognizeUtil
 *
 * @author yizzuide
 * @since 1.13.9
 * Create at 2019/10/29 15:49
 */
public class RecognizeUtil {
    /**
     * 是否是复合类型
     * @param obj   源数据
     * @return  复合类型返回true
     */
    public static boolean isCompoundType(Object obj) {
        return !(obj instanceof Byte || obj instanceof Boolean || obj instanceof CharSequence ||
                obj instanceof Short || obj instanceof Integer || obj instanceof Long ||
                obj instanceof Double || obj instanceof Float);
    }
}
