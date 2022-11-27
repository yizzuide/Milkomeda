package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.service.IService;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformPage;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformQueryPageData;

import java.io.Serializable;
import java.util.List;
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
     * Remove record row before check it reference.
     * @param entity  entity which has key of id value
     * @return  false if it has referenced
     */
    boolean removeBeforeCheckRef(T entity);

    /**
     * Assign authority for owner
     * @param ownerId   owner id
     * @param itemIds   authority item id list
     * @param conditionProvider condition which find owner
     * @param generator create authority item row to owner
     * @return  true if success
     */
    boolean assignAuthority(Serializable ownerId, List<? extends Serializable> itemIds,
                            SFunction<T, Object> conditionProvider,
                            Function<Serializable, T> generator);
}
