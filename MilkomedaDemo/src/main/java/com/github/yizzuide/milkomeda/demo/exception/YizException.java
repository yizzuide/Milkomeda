package com.github.yizzuide.milkomeda.demo.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * YizException
 * 自定义异常
 *
 * @author yizzuide
 * Create at 2019/03/24 22:44
 */
@Getter
@Setter
public class YizException extends RuntimeException {
    private Long id;
    private String type;

    public YizException(Long id, String type, String message) {
        super(message);
        this.id = id;
        this.type = type;
    }
}
