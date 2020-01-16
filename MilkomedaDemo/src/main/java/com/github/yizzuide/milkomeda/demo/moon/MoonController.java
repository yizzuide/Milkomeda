package com.github.yizzuide.milkomeda.demo.moon;

import com.github.yizzuide.milkomeda.moon.Moon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * MoonController
 *
 * @author yizzuide
 * Create at 2019/12/31 18:55
 */
@RestController
@RequestMapping("/moon")
public class MoonController {
    @Autowired
    private Moon<String> moon;

    @RequestMapping("current")
    public String current() {
        return Moon.getPhase("go-123", moon);
    }
}
