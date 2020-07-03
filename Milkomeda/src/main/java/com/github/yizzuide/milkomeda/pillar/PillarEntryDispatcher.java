package com.github.yizzuide.milkomeda.pillar;

import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * PillarEntryDispatcher
 * 分流派发器
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/02 17:59
 */
@Slf4j
public class PillarEntryDispatcher {
    /**
     * 分流派发
     * @param tag   业务tag
     * @param code  code类型
     * @param args  参数
     * @param <T>   返回类型
     * @return  调用返回值
     */
    @SuppressWarnings("unchecked")
    public static <T> T dispatch(String tag, String code, Object ...args) {
        List<HandlerMetaData> metaDataList = PillarEntryContext.getPillarEntryMap().get(tag);
        for (HandlerMetaData handlerMetaData : metaDataList) {
            if (handlerMetaData.getAttributes().get(PillarEntryContext.ATTR_CODE).equals(code)) {
                try {
                    return (T) handlerMetaData.getMethod().invoke(handlerMetaData.getTarget(), args);
                } catch (Exception e) {
                    log.error("Pillar entry dispatch error with msg: {}", e.getMessage(), e);
                }
            }
        }
        throw new IllegalArgumentException("Pillar entry dispatch error with tag[" + tag + "] and code [" + code + "]");
    }
}
