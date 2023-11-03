package com.github.yizzuide.milkomeda.demo.crust.controller;

import com.github.yizzuide.milkomeda.crust.CrustContext;
import com.github.yizzuide.milkomeda.crust.CrustPermission;
import com.github.yizzuide.milkomeda.crust.CrustUserInfo;
import com.github.yizzuide.milkomeda.demo.crust.pojo.User;
import com.github.yizzuide.milkomeda.hydrogen.uniform.ResultVO;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformResult;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.wf.captcha.SpecCaptcha;
import com.wf.captcha.base.Captcha;
import com.wf.captcha.utils.CaptchaUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;

/**
 * LoginController
 *
 * @author yizzuide
 * <br>
 * Create at 2019/11/11 23:52
 */
@RestController
public class LoginController {

    @GetMapping("verifyCode/render")
    public void render(HttpServletRequest request, HttpServletResponse response) throws IOException, FontFormatException {
        // 设置请求头为输出图片类型
        response.setContentType(MediaType.IMAGE_PNG_VALUE);
        response.setHeader(HttpHeaders.PRAGMA, "No-cache");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
        response.setDateHeader(HttpHeaders.EXPIRES, 0);

        SpecCaptcha captcha = new SpecCaptcha(80, 40, 4);
        captcha.setFont(new Font("Arial", Font.PLAIN, 24));
        // 字符+数字组合
        captcha.setCharType(Captcha.TYPE_DEFAULT);
        CaptchaUtil.out(captcha, request, WebContext.getRawResponse());

        // 验证输入
        //CaptchaUtil.ver(code, request);
    }

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
