package com.github.yizzuide.milkomeda.demo.moon;

import com.github.yizzuide.milkomeda.moon.Moon;
import com.github.yizzuide.milkomeda.moon.PercentMoonStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MoonConfig
 *
 * @author yizzuide
 * Create at 2019/12/31 18:56
 */
@Configuration
public class MoonConfig {
    @Bean("smsMoon")
    public Moon<String> smsMoon() {
        Moon<String> moon = new Moon<>();
        moon.add("七牛云短信", "阿里云短信", "容联云短信");
        return moon;
    }


    @Bean("abTestMoon")
    public Moon<Integer> abTestMoon() {
        Moon<Integer> moon = new Moon<>();
        moon.setMoonStrategy(new PercentMoonStrategy());
        // AB测试：15%为0，85为1
        moon.add(15, 85);
        return moon;
    }
}
