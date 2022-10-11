package com.github.yizzuide.milkomeda.demo.sundial.service.impl;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.demo.sundial.mapper.TOrder2Mapper;
import com.github.yizzuide.milkomeda.demo.sundial.service.DataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author jsq 786063250@qq.com
 * <br>
 * Create at 2020/5/9
 */
@Slf4j
@Service
public class DataSourceServiceImpl implements DataSourceService {

    @Resource
    private TOrder2Mapper tOrder2Mapper;

    @Override
    public int insert(TOrder tOrder) {
        int effectCount = tOrder2Mapper.insert(tOrder);
        // 测试多数据源事务
        List<TOrder> tOrders = queryAll(new TOrder());
        log.info("query list: {}", tOrders);
        return effectCount;
    }

    @Override
    public TOrder queryByOrderNo(Long orderNo) {
        return tOrder2Mapper.findByOrderNo(orderNo);
    }

    @Override
    public List<TOrder> queryAll(TOrder tOrder) {
        return tOrder2Mapper.queryAll(tOrder);
    }
}
