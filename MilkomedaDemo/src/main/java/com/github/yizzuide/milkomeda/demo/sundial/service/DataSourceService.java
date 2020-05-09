package com.github.yizzuide.milkomeda.demo.sundial.service;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;

import java.util.List;

/**
 * @date: 2020/5/9
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe:
 */
public interface DataSourceService {

    /**
     * 新增数据
     * @param tOrder
     * @return
     */
    int insert(TOrder tOrder);

    List<TOrder> queryAll(TOrder tOrder);
}
