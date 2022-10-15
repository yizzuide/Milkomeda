package com.github.yizzuide.milkomeda.demo.crust.controller;

import com.github.yizzuide.milkomeda.crust.CrustContext;
import com.github.yizzuide.milkomeda.crust.CrustUserInfo;
import com.github.yizzuide.milkomeda.demo.crust.pojo.User;
import com.github.yizzuide.milkomeda.hydrogen.uniform.ResultVO;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * LoginController
 *
 * @author yizzuide
 * <br>
 * Create at 2019/11/11 23:52
 */
@RestController
public class LoginController {

    @PostMapping("login")
    public ResultVO<CrustUserInfo<User>> login(String username, String password) {
        return UniformResult.ok(CrustContext.get().login(username, password, User.class));
    }

    @GetMapping("refresh")
    public ResultVO<CrustUserInfo<User>> refresh() {
        CrustUserInfo<User> userInfo = CrustContext.get().getUserInfo(User.class);
        String token = CrustContext.get().refreshToken();
        userInfo.setToken(token);
        return UniformResult.ok(userInfo);
    }
}
