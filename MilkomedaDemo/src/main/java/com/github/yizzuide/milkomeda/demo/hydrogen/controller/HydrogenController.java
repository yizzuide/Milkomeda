package com.github.yizzuide.milkomeda.demo.hydrogen.controller;

import com.github.yizzuide.milkomeda.demo.hydrogen.exception.YizException;
import com.github.yizzuide.milkomeda.demo.hydrogen.service.TOrderService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    public String testTx(@RequestParam("id") Integer id) {
        // 模拟系统异常
        // tOrderService.testTx();
        // 自定义异常
        throw new YizException(1001L, "test", "测试异常");
        // return HttpStatus.OK.name();
    }
}
