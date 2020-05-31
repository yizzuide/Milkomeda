package com.github.yizzuide.milkomeda.demo.sundial.service.impl;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.demo.halo.mapper.TOrderMapper;
import com.github.yizzuide.milkomeda.demo.sundial.service.DataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jsq 786063250@qq.com
 * Create at 2020/5/9
 */
@Slf4j
@Service
public class DataSourceServiceImpl implements DataSourceService {

    @Resource
    private TOrderMapper tOrderMapper;

    @Override
    public int insert(TOrder tOrder) {
        int effectCount = tOrderMapper.insert(tOrder);
        List<TOrder> tOrders = queryAll(new TOrder());
        log.info("query list: {}", tOrders);
        return effectCount;
    }

    @Override
    public List<TOrder> queryAll(TOrder tOrder) {
        return tOrderMapper.queryAll(tOrder);
    }
}
