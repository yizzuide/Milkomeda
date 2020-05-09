package com.github.yizzuide.milkomeda.demo.sundial;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.demo.halo.mapper.TOrderMapper;
import com.github.yizzuide.milkomeda.demo.sundial.service.DataSourceService;
import com.github.yizzuide.milkomeda.fusion.Fusion;
import com.github.yizzuide.milkomeda.sundial.SundialDynamicDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.util.Date;

/**
 * @date: 2020/5/8
 * @author: jsq
 * @email: 786063250@qq.com
 * @describe:
 */
@Slf4j
@RestController
@RequestMapping("sundial")
public class SundialController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private TOrderMapper tOrderMapper;

    
    @Fusion
    @RequestMapping("get")
    public Object getData() {
        log.info("get datasource: {}", dataSource.getClass());
//        TOrder tOrder = new TOrder();
//        tOrder.setOrderNo(123L);
//        tOrder.setCreateTime(new Date());
//        tOrder.setProductId(1L);
//        tOrder.setProductName("测试");
//        tOrder.setUserId(222L);
//        dataSourceService.insert(tOrder);
        return  dataSourceService.queryAll(new TOrder());
    }


    @RequestMapping("slave")
    public Object slave() {
        log.info("get datasource: {}", dataSource.getClass());
//        TOrder tOrder = new TOrder();
//        tOrder.setOrderNo(123L);
//        tOrder.setCreateTime(new Date());
//        tOrder.setProductId(1L);
//        tOrder.setProductName("测试");
//        tOrder.setUserId(222L);
//        dataSourceService.insert(tOrder);

        return  tOrderMapper.queryAll(new TOrder());
    }
}
