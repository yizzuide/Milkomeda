package com.github.yizzuide.milkomeda.test;

import com.github.yizzuide.milkomeda.demo.MilkomedaDemoApplication;
import com.github.yizzuide.milkomeda.moon.Moon;
import lombok.extern.slf4j.Slf4j;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Lazy;
import org.springframework.test.context.junit4.SpringRunner;

import jakarta.annotation.Resource;

/**
 * MoonTest
 *
 * @author yizzuide
 * <br>
 * Create at 2020/05/28 10:55
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MilkomedaDemoApplication.class)
public class MoonTest {
    @Lazy
    @Resource
    private Moon<Integer> abTestMoon;

    // 引入性能测试规则
    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();

    // 10个线程调用10次
    @PerfTest(invocations = 10, threads = 10)
    @Test
    public void testAbTest() {
        Integer phase1 = Moon.getPhase("ab-123", abTestMoon);
        log.info("ab-123: {}",  phase1);
        // 更换key来进入第二个环（环与环相互隔离）
        Integer phase2 = Moon.getPhase("ab-456", abTestMoon);
        log.info("ab-456: {}",  phase2);
    }
}
