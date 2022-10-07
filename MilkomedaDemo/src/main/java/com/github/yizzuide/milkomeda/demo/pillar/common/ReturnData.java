package com.github.yizzuide.milkomeda.demo.pillar.common;

import lombok.Data;

/**
 * ReturnData
 * 响应对象
 *
 * @author yizzuide
 * <br />
 * Create at 2019/04/11 16:54
 */
@Data
public class ReturnData {
    private String code;
    private boolean success;
    private String msg;
}
