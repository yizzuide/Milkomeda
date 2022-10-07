package com.github.yizzuide.milkomeda.demo.hydrogen.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * YizException
 * 自定义异常
 *
 * @author yizzuide
 * <br />
 * Create at 2019/03/24 22:44
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class YizException extends RuntimeException {
    private static final long serialVersionUID = -8117216222519632116L;
    private Long code;
    private String type;

    public YizException(Long id, String type, String message) {
        super(message);
        this.code = id;
        this.type = type;
    }
}
