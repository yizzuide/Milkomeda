package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
     * Type to match and query.
     * @return PrefectType
     * @since 3.15.0
     */
    @AliasFor("prefect")
    PrefectType value() default PrefectType.EQ;

    /**
     * Type to match and query.
     * @return PrefectType
     *
     */
    @AliasFor("value")
    PrefectType prefect() default PrefectType.EQ;

    /**
     * Custom type of query match with {@link PageableService#additionParseQueryMatcher(QueryWrapper, String, String, boolean, Object)}.
     * @return string of a prefect type
     */
    String prefectString() default "";

    /**
     * Query result list order type.
     * @return  true if you need order with asc
     */
    boolean forward() default true;

    /**
     * Sort order if used with multiple fields.
     * @return the 0 is natural, -1 is higher, 1 is lower
     * @since 4.0.0
     */
    int order() default 0;

    /**
     * Query link field with `targetNameField` of  {@link QueryLinker}.
     * @return match data field name
     */
    String matchDataField() default "";

    /**
     * Query match data fields for `queryMatchData` params of {@link IPageableService}.
     * @return query field names
     * @since 4.0.0
     */
    String[] queryFields() default {};

    /**
     * Bundle conditions in a group.
     * @return  group name
     * @since 3.15.0
     */
    String[] group() default { IPageableService.DEFAULT_GROUP };
}
