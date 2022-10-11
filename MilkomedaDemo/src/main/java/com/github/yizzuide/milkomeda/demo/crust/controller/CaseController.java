package com.github.yizzuide.milkomeda.demo.crust.controller;

import com.github.yizzuide.milkomeda.crust.CrustContext;
import com.github.yizzuide.milkomeda.crust.CrustUserInfo;
import com.github.yizzuide.milkomeda.demo.crust.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * CaseController
 *
 * @author yizzuide
 * <br>
 * Create at 2019/11/12 00:09
 */
@Slf4j
@RestController
@RequestMapping("case")
public class CaseController {

    @GetMapping("info")
//    @PreAuthorize("hasAuthority('ROLE_USER')")
    // 和上面等同
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> info() {
        CrustUserInfo<User> userInfo = CrustContext.getUserInfo(User.class);
        log.info("userInfo: {}", userInfo);
        Map<String, Object> data = new HashMap<>();
        data.put("id", "12345667009874");
        data.put("name", "case-01");

        CrustUserInfo<User> userInfo2 = CrustContext.getUserInfo(User.class);
        log.info("比较两个对象：{}", userInfo == userInfo2);
        return data;
    }

    @GetMapping("find/{id}")
    public Map<String, Object> find(@PathVariable("id") Long id) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("name", "case-01");
        return data;
    }
}
