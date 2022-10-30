package com.github.yizzuide.milkomeda.sirius;

import java.lang.annotation.*;

/**
 * Query matcher using for {@link}
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/30 16:58
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
public @interface QueryMatcher {
    /**
     * What type of query can match.
     * @return PrefectType
     */
    PrefectType prefect() default PrefectType.EQ;

    /**
     * Query result list is forward type.
     * @return  true if you need forward
     */
    boolean forward() default true;
}
