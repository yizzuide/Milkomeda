package com.github.yizzuide.milkomeda.demo.crust.controller;

import com.github.yizzuide.milkomeda.crust.CrustAnon;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AnonController
 *
 * @author yizzuide
 * <br />
 * Create at 2020/05/13 11:36
 */
@RequestMapping("anon")
@RestController
public class AnonController {

    @CrustAnon
    @RequestMapping("test")
    public Object test() {
        return "OK";
    }
}
