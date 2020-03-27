package com.github.yizzuide.milkomeda.demo.hydrogen.controller;

import com.github.yizzuide.milkomeda.demo.hydrogen.exception.YizException;
import com.github.yizzuide.milkomeda.demo.hydrogen.service.TOrderService;
import com.github.yizzuide.milkomeda.demo.hydrogen.vo.UserVO;
import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenHolder;
import com.github.yizzuide.milkomeda.hydrogen.validator.PhoneConstraint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * HydrogenController
 *
 * @author yizzuide
 * Create at 2020/03/25 21:48
 */
@Validated // 让方法参数上的校验注解生效，需要在Controller添加@Validated
@Slf4j
@RequestMapping("hydrogen")
@RestController
public class HydrogenController {

    @Resource
    private TOrderService tOrderService;

    @RequestMapping("tx")
    public String testTx(@RequestParam("id") Integer id) {
        tOrderService.testTx();
        return HttpStatus.OK.name();
    }

    @RequestMapping("uniform")
    public void testUniform() {
        // 自定义异常
        throw new YizException(1001L, "test", "测试异常");
    }

    @RequestMapping("valid")
    public String valid(@PhoneConstraint String phone) {
        log.info("valid phone:  {}", phone);
        return HttpStatus.OK.name();
    }

    @RequestMapping("register")
    public String register(@Valid UserVO userVO) {
        log.info("valid userVO:  {}", userVO);
        return HttpStatus.OK.name();
    }

    // 中国: 127.0.0.1:8091/hydrogen/i18n or 127.0.0.1:8091/hydrogen/i18n?lang=
    // 印尼：127.0.0.1:8091/hydrogen/i18n?lang=en_ID
    @RequestMapping("i18n")
    public String i18n() {
        return HydrogenHolder.getI18nMessages().getWithParam("operation.success", "OK");
    }
}
