package com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.model.RmOrder;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.service.RmOrderService;
import com.github.yizzuide.milkomeda.demo.molecule.eventsourcing.infrastructure.orm.mapper.RmOrderMapper;
import org.springframework.stereotype.Service;

/**
* @author yizzuide
* @description 针对表【rm_order】的数据库操作Service实现
* @createDate 2025-06-13 17:37:36
*/
@Service
public class RmOrderServiceImpl extends ServiceImpl<RmOrderMapper, RmOrder>
    implements RmOrderService{

}




