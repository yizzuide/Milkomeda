package com.github.yizzuide.milkomeda.comet.logger;

import lombok.Data;

/**
 * CometUrlLogData
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/28 12:19
 */
@Data
public class CometLoggerData {
    private String uri;
    private String method;
    private String params;
    private String token;
    private String uid;
}
