package com.github.yizzuide.milkomeda.pillar;

import java.lang.reflect.Method;

/**
 * PillarRecognizer
 * Pillar分流柱识别器，配合 PillarType 使用
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 1.6.0
 * Create at 2019/04/11 18:02
 */
public class PillarRecognizer {
    /**
     * 通过反射从枚举类识别Pillar分流柱类型（效率低）
     * @param enumClazz 枚举类型
     * @param identifier 标识符
     * @return Pillar分流柱类型
     * @throws Exception 反射调用异常
     */
    public static String typeOf(Class<? extends PillarType> enumClazz, Object identifier) throws Exception {
        Method method = enumClazz.getMethod("values");
        PillarType[] pillarTypes = (PillarType[]) method.invoke(null);
        return typeOf(pillarTypes, identifier);
    }

    /**
     * 通过Pillar类型ID匹配类型名（效率高）
     * @param typeValues    PillarType所有枚举值
     * @param identifier    标识符
     * @return  Pillar分流柱类型
     */
    public static String typeOf(PillarType[] typeValues, Object identifier) {
        for (PillarType pillarType : typeValues) {
            if (String.valueOf(identifier).equals(String.valueOf(pillarType.identifier()))) {
                return pillarType.pillarType();
            }
        }
        return null;
    }
}
