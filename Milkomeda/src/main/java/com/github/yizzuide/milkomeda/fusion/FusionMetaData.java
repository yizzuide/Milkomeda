package com.github.yizzuide.milkomeda.fusion;

import lombok.Builder;
import lombok.Data;

/**
 * FusionMetaData
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/05 16:28
 */
@Builder
@Data
public class FusionMetaData<T> {
    /**
     * 返回值
     */
    private T returnData;
    /**
     * 是否业务错误
     */
    private boolean error;
    /**
     * 显示消息
     */
    private String msg;
}
