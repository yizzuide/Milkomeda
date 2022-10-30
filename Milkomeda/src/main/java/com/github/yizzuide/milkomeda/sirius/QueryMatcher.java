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
     * Type to match query.
     * @return PrefectType
     */
    PrefectType prefect() default PrefectType.EQ;

    /**
     * Custom type of query match with {@link PageableService#additionParseQueryMatcher(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper, java.lang.String, java.lang.String, boolean, java.lang.Object)}.
     * @return string of prefect type
     */
    String prefectString() default "";

    /**
     * Query result list is forward type.
     * @return  true if you need forward
     */
    boolean forward() default true;
}
