package com.github.yizzuide.milkomeda.sirius;

import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformQueryPageData;

/**
 * Prefect type using for {@link QueryMatcher}
 *
 * @since 3.14.0
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
     * Match empty field (null or '').
     */
    EMPTY,

    /**
     * Match using sql `in`.
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
    OrderByPre,

    /**
     * Page result list order by.
     */
    OrderByPost,

    /**
     * Using for `startDate` and `endDate` of {@link UniformQueryPageData} query.
     */
    PageDate,

    /**
     * Query with unix time relative to {@link PrefectType#PageDate}.
     */
    PageUnixTime
}
