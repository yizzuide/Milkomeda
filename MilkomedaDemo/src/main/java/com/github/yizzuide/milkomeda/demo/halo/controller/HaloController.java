package com.github.yizzuide.milkomeda.demo.halo.controller;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.demo.halo.mapper.TOrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * HaloController
 *
 * @author yizzuide
 * Create at 2020/01/30 20:15
 */
@RequestMapping("/halo")
@RestController
public class HaloController {

    @Autowired
    private TOrderMapper tOrderMapper;

    @RequestMapping("order/{id}")
    public TOrder getOrder(@PathVariable("id") Long id) {
        return tOrderMapper.queryById(id);
    }

    @RequestMapping("order/all")
    public List<TOrder> getAllOrder() {
        return tOrderMapper.queryAll(null);
    }

    @RequestMapping("order/offer")
    public String getOrder() {
        TOrder tOrder = new TOrder();
        tOrder.setUserId(2L);
        tOrder.setOrderNo(1435436546556098L);
        tOrder.setProductId(2L);
        tOrder.setProductName("产品x");
        tOrder.setPrice(100000L);
        Date now = new Date();
        tOrder.setCreateTime(now);
        tOrder.setUpdateTime(now);
        tOrderMapper.insert(tOrder);
         return "OK";
    }
}
