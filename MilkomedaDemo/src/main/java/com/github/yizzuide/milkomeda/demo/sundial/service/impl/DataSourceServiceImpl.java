package com.github.yizzuide.milkomeda.demo.sundial.service.impl;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.demo.halo.mapper.TOrderMapper;
import com.github.yizzuide.milkomeda.demo.sundial.service.DataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @date: 2020/5/9
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe:
 */
@Service
public class DataSourceServiceImpl implements DataSourceService {


    @Autowired
    private TOrderMapper tOrderMapper;

    @Override
    public int insert(TOrder tOrder) {

        return     tOrderMapper.insert(tOrder);
    }

    @Override
    public List<TOrder> queryAll(TOrder tOrder) {
        return tOrderMapper.queryAll(tOrder);
    }
}
