package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformPage;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformQueryPageData;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A pageable service extends from IService
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/30 17:35
 */
public interface IPageableService<T> extends IService<T> {
    /**
     * Query by page data.
     * @param queryPageData page data
     * @return  UniformPage
     */
    UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData);

    /**
     * Query by page data and match group.
     * @param queryPageData page data
     * @param group match group name
     * @return  UniformPage
     * @since 3.15.0
     */
    UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData, String group);

    /**
     * Query by page data and match data.
     * @param queryPageData page data
     * @param queryMatchData match data
     * @return  UniformPage
     * @since 3.15.0
     */
    UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData, Map<String, Object> queryMatchData);

    /**
     * Query by page data, match data and group name.
     * @param queryPageData page data
     * @param queryMatchData match data
     * @param group match group name
     * @return  UniformPage
     * @since 3.15.0
     */
    UniformPage<T> selectByPage(UniformQueryPageData<T> queryPageData, Map<String, Object> queryMatchData, String group);

    /**
     * Remove record row before check it reference.
     * @param entity  entity which has key of id value
     * @return  false if it has referenced
     */
    boolean removeBeforeCheckRef(T entity);

    /**
     * Assign authority for the owner.
     * @param ownerId   owner id
     * @param itemIds   authority item id list
     * @param conditionProvider condition which find linked with the owner
     * @param generator create authority item row linked with the owner
     * @return  true if success
     */
    boolean assignAuthority(Serializable ownerId, List<? extends Serializable> itemIds,
                            SFunction<T, Object> conditionProvider,
                            Function<Serializable, T> generator);
}
