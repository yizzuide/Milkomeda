package com.github.yizzuide.milkomeda.comet;

import lombok.Data;

/**
 * CometUrlLogData
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/28 12:19
 */
@Data
public class CometUrlLogData {
    private String uri;
    private String method;
    private String params;
    private String token;
    private String uid;
}
