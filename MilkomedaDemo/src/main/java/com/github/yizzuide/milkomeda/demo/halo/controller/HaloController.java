package com.github.yizzuide.milkomeda.demo.halo.controller;

import com.github.yizzuide.milkomeda.demo.halo.domain.TOrder;
import com.github.yizzuide.milkomeda.demo.halo.service.TOrderService;
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
    private TOrderService orderService;

    @RequestMapping("order/{id:\\d+}")
    public TOrder getOrder(@PathVariable("id") Long id) {
        return orderService.queryByIdAndTime(id, new Date());
    }

    @RequestMapping("order/all")
    public List<TOrder> getAllOrder() {
        return orderService.queryAll(null);
    }

    @RequestMapping("order/offer")
    public String getOrder() {
        orderService.save();
         return "OK";
    }
}
