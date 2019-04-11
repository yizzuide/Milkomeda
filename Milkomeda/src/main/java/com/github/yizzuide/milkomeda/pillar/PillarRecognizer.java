package com.github.yizzuide.milkomeda.pillar;

import java.lang.reflect.Method;

/**
 * PillarRecognizer
 * Pillar逻辑处理单元柱识别器，配合 PillarType 使用
 *
 * @author yizzuide
 * @since 0.2.0
 * Create at 2019/04/11 18:02
 */
public class PillarRecognizer {
    /**
     * 从枚举类识别Pillar逻辑单元柱类型
     * @param enumClazz 枚举类型
     * @param identifier 标识符
     * @return Pillar逻辑单元柱类型
     * @throws Exception 反射调用异常
     */
    public static String typeOf(Class<?> enumClazz, Object identifier) throws Exception {
        PillarType pillarType = (PillarType) typeOfEnum(enumClazz, identifier);
        return pillarType != null ? pillarType.pillarType() : null;
    }

    /**
     * 根据标识符识别枚举类
     * @param enumClazz 枚举类型
     * @param identifier 标识符
     * @param <T> 返回枚举类型
     * @return Pillar逻辑单元柱类型
     * @throws Exception 反射调用异常
     */
    @SuppressWarnings("unchecked")
    public static <T> T typeOfEnum(Class<T> enumClazz, Object identifier) throws Exception {
        Method method = enumClazz.getMethod("values");
        PillarType[] pillarTypes = (PillarType[]) method.invoke(null);
        for (PillarType pillarType : pillarTypes) {
            if (identifier.equals(pillarType.identifier())) {
                return (T) pillarType;
            }
        }
        return null;
    }
}
