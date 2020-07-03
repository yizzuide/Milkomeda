package com.github.yizzuide.milkomeda.pillar;

import java.lang.annotation.*;

/**
 * PillarEntryPoint
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/02 17:20
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface PillarEntryPoint {
    /**
     * 业务区分tag，会覆盖{@link PillarEntryHandler#tag()}
     * @return  String
     */
    String tag() default "";

    /**
     * 执行入口标识
     * @return  String
     */
    String code();
}
