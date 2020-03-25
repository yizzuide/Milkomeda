package com.github.yizzuide.milkomeda.demo.hydrogen.controller;

import com.github.yizzuide.milkomeda.demo.hydrogen.service.TOrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * HydrogenController
 *
 * @author yizzuide
 * Create at 2020/03/25 21:48
 */
@RequestMapping("hydrogen")
@RestController
public class HydrogenController {

    @Resource
    private TOrderService tOrderService;

    @RequestMapping("tx")
    public String testTx() {
        tOrderService.testTx();
        return HttpStatus.OK.name();
    }
}
