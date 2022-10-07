package com.github.yizzuide.milkomeda.demo.sundial.service;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;

import java.util.List;

/**
 * @author jsq 786063250@qq.com
 * <br />
 * Create at 2020/5/9
 */
public interface DataSourceService {

    int insert(TOrder tOrder);

    TOrder queryByOrderNo(Long orderNo);

    List<TOrder> queryAll(TOrder tOrder);
}
