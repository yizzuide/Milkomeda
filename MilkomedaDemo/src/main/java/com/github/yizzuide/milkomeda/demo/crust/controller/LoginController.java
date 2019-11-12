package com.github.yizzuide.milkomeda.demo.crust.controller;

import com.github.yizzuide.milkomeda.crust.CrustContext;
import com.github.yizzuide.milkomeda.crust.CrustUserInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LoginController
 *
 * @author yizzuide
 * Create at 2019/11/11 23:52
 */
@RestController
public class LoginController {

    @PostMapping("login")
    public CrustUserInfo login(String username, String password) {
        return CrustContext.get().login(username, password);
    }

    @GetMapping("refresh")
    public CrustUserInfo refresh() {
        CrustUserInfo userInfo = CrustContext.get().getUserInfo();
        String token = CrustContext.get().refreshToken();
        userInfo.setToken(token);
        return userInfo;
    }
}
