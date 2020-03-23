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
        // 设置缓存缓存实例名（需要在milkomeda.light.instances下配置不同的缓存实例）
        moon.setCacheName("ab-test");
        PercentMoonStrategy moonStrategy = new PercentMoonStrategy();
        // 设置百分比总量（默认为100)
        moonStrategy.setPercent(10);
        moon.setMoonStrategy(moonStrategy);
        // AB测试阶段值：15%为0，85%为1
//        moon.add(PercentMoonStrategy.parse("15/85"));
        moon.add(PercentMoonStrategy.parse("3/7"));
        return moon;
    }
}
