package com.github.yizzuide.milkomeda.demo.sundial;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.demo.sundial.service.DataSourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    private DataSourceService dataSourceService;

    @RequestMapping("get/{orderNo}")
    public Object getData(@PathVariable Long orderNo) {
        return  dataSourceService.queryByOrderNo(orderNo);
    }

    @RequestMapping("add/{orderNo}")
    public Object add(@PathVariable("orderNo") Long orderNo) {
        TOrder tOrder = new TOrder();
        tOrder.setOrderNo(orderNo);
        tOrder.setCreateTime(new Date());
        tOrder.setProductId(280L);
        tOrder.setProductName("小玲");
        tOrder.setUserId(2L);
        dataSourceService.insert(tOrder);
        return tOrder;
    }
}
