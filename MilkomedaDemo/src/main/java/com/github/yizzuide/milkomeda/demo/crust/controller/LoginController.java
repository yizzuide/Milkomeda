package com.github.yizzuide.milkomeda.demo.crust.controller;

import com.github.yizzuide.milkomeda.crust.CrustContext;
import com.github.yizzuide.milkomeda.crust.CrustPermission;
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
    public ResultVO<CrustUserInfo<User, CrustPermission>> login(String username, String password) {
        return UniformResult.ok(CrustContext.get().login(username, password, User.class));
    }

    @GetMapping("refresh")
    public ResultVO<CrustUserInfo<?, CrustPermission>> refresh() {
        CrustUserInfo<?, CrustPermission> userInfo = CrustContext.get().refreshToken();
        return UniformResult.ok(userInfo);
    }
}
