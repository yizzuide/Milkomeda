package com.github.yizzuide.milkomeda.sirius;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Query matcher using for {@link IPageableService}.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/30 16:58
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.ANNOTATION_TYPE})
@Inherited
@Repeatable(QueryMatchers.class)
public @interface QueryMatcher {
    /**
     * Type to match query.
     * @return PrefectType
     * @since 3.15.0
     */
    @AliasFor("prefect")
    PrefectType value() default PrefectType.EQ;

    /**
     * Type to match query.
     * @return PrefectType
     *
     */
    @AliasFor("value")
    PrefectType prefect() default PrefectType.EQ;

    /**
     * Custom type of query match with {@link PageableService#additionParseQueryMatcher(com.baomidou.mybatisplus.core.conditions.query.QueryWrapper, java.lang.String, java.lang.String, boolean, java.lang.Object)}.
     * @return string of prefect type
     */
    String prefectString() default "";

    /**
     * Query result list order type.
     * @return  true if you need order with asc
     */
    boolean forward() default true;

    /**
     * Query link field with `targetNameField` of  {@link QueryLinker}.
     * @return match data field name
     */
    String matchDataField() default "";

    /**
     * Bundle conditions in group.
     * @return  group name
     * @since 3.15.0
     */
    String[] group() default { "default" };
}
