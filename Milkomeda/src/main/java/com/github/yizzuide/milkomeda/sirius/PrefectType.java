package com.github.yizzuide.milkomeda.sirius;

import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformQueryPageData;

/**
 * Prefect type using for {@link QueryMatcher}
 *
 * @since 3.14.0
 * @version 4.0.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/30 16:59
 */
public enum PrefectType {
    /**
     * Equals field.
     */
    EQ,

    /**
     * Not Equals field.
     */
    NEQ,

    /**
     * Greater than field.
     */
    GT,

    /**
     * Greater than or equals field.
     */
    GE,

    /**
     * Less than field.
     */
    LT,

    /**
     * Less than or equals field.
     */
    LE,

    /**
     * Match empty field (null or '').
     */
    EMPTY,

    /**
     * Match using sql `in($1%)` or
     * Match and query target field value of {@link QueryLinker} by `like`, and return linker entity ids for sql `in($1)`.
     */
    IN,

    /**
     * Match and query target field value of {@link QueryLinker} by `eq`, and return linker entity ids for sql `in($1)`.
     */
    LINK_EQ_IN,

    /**
     * Match using sql `like $1%`.
     */
    LIKE,

    /**
     * Match using sql `between $1 and $2`.
     */
    BETWEEN,

    /**
     * Match using sql `order by $1`.
     */
    OrderBy,

    /**
     * Using for `startDate` and `endDate` of {@link UniformQueryPageData} query.
     */
    PageDate,

    /**
     * Query with unix time relative to {@link PrefectType#PageDate}.
     */
    PageUnixTime
}
