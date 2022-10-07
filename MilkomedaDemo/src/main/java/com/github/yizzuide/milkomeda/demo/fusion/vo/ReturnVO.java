package com.github.yizzuide.milkomeda.demo.fusion.vo;

import lombok.Data;

/**
 * ReturnVO
 *
 * @author yizzuide
 * <br />
 * Create at 2020/01/03 18:39
 */
@Data
public class ReturnVO<T> {
    private String code;
    private String msg;
    private T data;

    public ReturnVO<T> ok(T data) {
        code = "0";
        this.data = data;
        return this;
    }

    public ReturnVO<T> error(String msg) {
        code = "-1";
        this.msg = msg;
        return this;
    }
}
