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
     * Match fuzzy with {@link QueryLinker} or not used linker using sql `in`.
     */
    IN,

    /**
     * Match full words with {@link QueryLinker} and using sql `in`.
     */
    LINK_EQ_IN,

    /**
     * Match using sql `like`.
     */
    LIKE,

    /**
     * Match using sql `order by`.
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
