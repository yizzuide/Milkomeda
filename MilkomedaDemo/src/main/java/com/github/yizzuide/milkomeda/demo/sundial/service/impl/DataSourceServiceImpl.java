package com.github.yizzuide.milkomeda.demo.sundial.service.impl;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.demo.halo.mapper.TOrderMapper;
import com.github.yizzuide.milkomeda.demo.sundial.service.DataSourceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jsq 786063250@qq.com
 * Create at 2020/5/9
 */
@Service
public class DataSourceServiceImpl implements DataSourceService {

    @Resource
    private TOrderMapper tOrderMapper;

    @Override
    public int insert(TOrder tOrder) {
        return tOrderMapper.insert(tOrder);
    }

    @Override
    public List<TOrder> queryAll(TOrder tOrder) {
        return tOrderMapper.queryAll(tOrder);
    }
}
