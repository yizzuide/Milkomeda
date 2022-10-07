/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
 * @version 3.11.6
 * <br />
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
                    return null;
                }
            }
        }
        throw new IllegalArgumentException("Pillar entry dispatch error with tag[" + tag + "] and code [" + code + "]");
    }
}
