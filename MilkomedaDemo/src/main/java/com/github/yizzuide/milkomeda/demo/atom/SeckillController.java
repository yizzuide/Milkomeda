package com.github.yizzuide.milkomeda.demo.atom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SeckillController
 *
 * @author yizzuide
 * Create at 2020/05/07 00:12
 */
@RestController
@RequestMapping("seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @RequestMapping("apply")
    public String seckill() {
        boolean result = seckillService.seckill(1001L, 10L);
        return result ? "OK" : "Fail";
    }
}
