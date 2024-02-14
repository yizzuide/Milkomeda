package com.github.yizzuide.milkomeda.demo.jupiter;

import com.github.yizzuide.milkomeda.jupiter.JupiterRuleEngine;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * PayController
 *
 * @author yizzuide
 * <br>
 * Create at 2020/05/20 00:06
 */
@RestController
@RequestMapping("jupiter")
public class JupiterController {

    // yml配置的实体需要使用@Lazy!!!
    @Lazy
    @Resource
    private JupiterRuleEngine jupiterRuleEngine;

    @RequestMapping("pay")
    public String pay() {
        // 设置Request Attribute域
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("uid", 2);
        WebContext.getRequest().setAttribute("userInfo", userInfo);

        // 运行放款风控规则引擎
        if (!jupiterRuleEngine.run("payRule")) {
            return "Fail";
        }
        // 放款操作。。
        return "OK";
    }
}
