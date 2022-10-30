package com.github.yizzuide.milkomeda.sirius;

import com.baomidou.mybatisplus.extension.service.IService;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformPage;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformQueryPageData;

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
}
