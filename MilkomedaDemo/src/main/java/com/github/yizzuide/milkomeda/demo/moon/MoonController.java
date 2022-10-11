package com.github.yizzuide.milkomeda.demo.moon;

import com.github.yizzuide.milkomeda.moon.Moon;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * MoonController
 *
 * @author yizzuide
 * <br>
 * Create at 2019/12/31 18:55
 */
@RestController
@RequestMapping("/moon")
public class MoonController {
    // 动态注册的bean，需要添加@Lazy
    @Lazy
    @Resource
    private Moon<String> smsMoon;
    @Lazy
    @Resource
    private Moon<Integer> abTestMoon;

    @RequestMapping("sms")
    public String sms() {
        // 不同的key所属自己的环
        String phase1 = Moon.getPhase("sms-123", smsMoon);
        String phase2 = Moon.getPhase("sms-456", smsMoon);
        return String.format("%s-%s", phase1, phase2);
    }

    @RequestMapping("abTest")
    public String abTest() {
        Integer phase1 = Moon.getPhase("ab-123", abTestMoon);
        Integer phase2 = Moon.getPhase("ab-456", abTestMoon);
        return String.format("%s-%s", phase1, phase2);
    }
}
