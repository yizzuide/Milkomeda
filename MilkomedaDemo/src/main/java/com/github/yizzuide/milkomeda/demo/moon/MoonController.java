package com.github.yizzuide.milkomeda.demo.moon;

import com.github.yizzuide.milkomeda.moon.Moon;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * MoonController
 *
 * @author yizzuide
 * Create at 2019/12/31 18:55
 */
@RestController
@RequestMapping("/moon")
public class MoonController {
    @Resource
    private Moon<String> smsMoon;
    @Resource
    private Moon<Integer> abTestMoon;

    @RequestMapping("sms")
    public String sms() {
        return Moon.getPhase("sms-123", smsMoon);
    }

    @RequestMapping("abTest")
    public Integer abTest() {
        return Moon.getPhase("ab-123", abTestMoon);
    }
}
