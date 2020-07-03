package com.github.yizzuide.milkomeda.pillar;

import java.lang.annotation.*;

/**
 * PillarEntryHandler
 *
 * @author yizzuide
 * @since 3.10.0
 * Create at 2020/07/02 17:52
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface PillarEntryHandler {
    /**
     * 业务区分tag
     * @return  String
     */
    String tag() default "";
}
