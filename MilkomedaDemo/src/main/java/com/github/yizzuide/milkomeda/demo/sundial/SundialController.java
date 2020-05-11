package com.github.yizzuide.milkomeda.demo.sundial;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.demo.halo.mapper.TOrderMapper;
import com.github.yizzuide.milkomeda.demo.sundial.service.DataSourceService;
import com.github.yizzuide.milkomeda.sundial.Sundial;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.Date;

/**
 * @author jsq 786063250@qq.com
 * Create at 2020/5/8
 */
@Slf4j
@RestController
@RequestMapping("sundial")
public class SundialController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DataSourceService dataSourceService;

    @Resource
    private TOrderMapper tOrderMapper;

    @RequestMapping("get")
    public Object getData() {
        log.info("get datasource: {}", dataSource.getClass());
        return  dataSourceService.queryAll(new TOrder());
    }

    @Sundial("read-only")
    @RequestMapping("get/readOnly")
    public Object slave() {
        log.info("get datasource: {}", dataSource.getClass());
        return  tOrderMapper.queryAll(new TOrder());
    }

    @RequestMapping("add")
    public Object add() {
        TOrder tOrder = new TOrder();
        tOrder.setOrderNo(123L);
        tOrder.setCreateTime(new Date());
        tOrder.setProductId(1L);
        tOrder.setProductName("测试");
        tOrder.setUserId(222L);
        dataSourceService.insert(tOrder);
        return tOrder;
    }
}
