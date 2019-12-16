package com.github.yizzuide.milkomeda.comet;

import java.lang.annotation.*;

/**
 * CometParam
 * API接口同时支持form表单数据和自定义消息体数据
 *
 * @author yizzuide
 * @since 2.0.0
 * Create at 2019/12/12 18:42
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface CometParam {
}
