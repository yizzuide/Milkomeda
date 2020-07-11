package com.github.yizzuide.milkomeda.demo.particle.controller;

import com.github.yizzuide.milkomeda.particle.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.Map;

/**
 * ParticleController
 *
 * @author yizzuide
 * Create at 2019/05/30 14:56
 */
@Slf4j
@RestController
@RequestMapping("particle")
public class ParticleController {

    @Resource
    private IdempotentLimiter idempotentLimiter;

    @Resource
    private TimesLimiter timesLimiter;

    @Resource
    private BarrierLimiter barrierLimiter;

    @Resource
    public BloomLimiter userBloomLimiter;

    @PostConstruct
    public void init() {
        // 添加测试数据
        userBloomLimiter.addAll(Collections.singletonList("15634256549"));
    }

    // http://localhost:8091/particle/exists?phone=15634256549
    // 判断用户有没有注册过
    @RequestMapping("exists")
    public String exists(String phone) {
        log.info("查询到用户：{}", phone);
        return phone;
    }

    @RequestMapping("check")
    public ResponseEntity<String> check(String token) throws Throwable {
        // 使用内置的方法限制一次请求的重复调用
        return idempotentLimiter.limit(token, 60L, (particle) -> {
            // 判断是否被限制
            if (particle.isLimited()) {
                return ResponseEntity.status(406).body("请求勿重复请求");
            }
            // 模拟业务处理耗时
            Thread.sleep(5000);
            return ResponseEntity.ok("ok");
        });
    }

    @RequestMapping("check2")
    // 注解的方式限制一次请求的重复调用（注：'#'为Spring EL表达式）
    // 注意：由于配置了限制器链，就有了两个去重限制器，由于框架内部根据类型查找，这里需要通过limiterBeanName指定
    @Limit(name = "user:check", key = "#token", expire = 60L, limiterBeanName = "idempotentLimiter")
    public ResponseEntity<String> check2(String token, Particle particle/*这个状态值自动注入*/) throws Throwable {
        log.info("check2: token={}", token);
        // 判断是否被限制
        if (particle.isLimited()) {
            return ResponseEntity.status(406).body("请求勿重复请求");
        }
        // 模拟业务处理耗时
        Thread.sleep(5000);
        return ResponseEntity.ok("ok");
    }

    @RequestMapping("check3")
    // 注解的方式限制一次请求的重复调用（#env获取自定义环境变量，#request获取请求对象）
    // 注意：由于配置了限制器链，就有了两个去重限制器，由于框架内部根据类型查找，这里需要通过limiterBeanName指定
    @Limit(name = "user:check", key = "#env['product'] + #request.getHeader('token')", expire = 60L, limiterBeanName = "idempotentLimiter")
    public ResponseEntity<String> check3(Particle particle/*这个状态值自动注入*/) throws Throwable {
        // 判断是否被限制
        log.info("limited: {}", particle.isLimited());
        if (particle.isLimited()) {
            return ResponseEntity.status(406).body("请求勿重复请求");
        }
        
        // 模拟业务处理耗时
        Thread.sleep(5000);
        
        return ResponseEntity.ok("ok");
    }


    @RequestMapping("send")
    public ResponseEntity<String> send(String phone) throws Throwable {
        // 使用内置的方法限制调用次数
        return timesLimiter.limit(phone, (particle) -> {
            if (particle.isLimited()) {
                return ResponseEntity.status(406).body("超过使用次数：" + particle.getValue() + "次");
            }
            return ResponseEntity.ok("发送成功，当前次数：" + particle.getValue());
        });
    }

    @RequestMapping("send2")
    // 注解的方式限制调用次数
    @Limit(name = "user:send", key = "#phone", limiterBeanClass = TimesLimiter.class)
    public ResponseEntity<String> send2(String phone, Particle particle/*这个状态值自动注入*/) {
        log.info("check2: phone={}", phone);
        if (particle.isLimited()) {
            return ResponseEntity.status(406).body("超过使用次数：" + particle.getValue() + "次");
        }
        return ResponseEntity.ok("发送成功，当前次数：" + particle.getValue());
    }

    // 方法嵌套调用的组合方式
    @RequestMapping("verify")
    public ResponseEntity<String> verify(String phone) throws Throwable {
        // 先请求重复检查
        return idempotentLimiter.limit(phone, 60L, (particle) -> {
            // 判断是否被限制
            if (particle.isLimited()) {
                return ResponseEntity.status(406).body("请求勿重复请求");
            }

            // 再检测次数
            return timesLimiter.limit(phone, (p) -> {
                if (p.isLimited()) {
                    return ResponseEntity.status(406).body("超过使用次数：" + p.getValue() + "次");
                }
                // 模拟业务处理耗时
                Thread.sleep(5000);

                return ResponseEntity.ok("发送成功，当前次数：" + p.getValue());
            });

        });
    }

    @RequestMapping("verify3")
    public ResponseEntity<String> verify3(String phone) throws Throwable {
        // 先请求重复检查
        return barrierLimiter.limit(phone, 60L, (particle) -> {
            // 判断是否被限制
            if (particle.isLimited()) {
                // 如果是去重限制类型
                if (particle.getType() == IdempotentLimiter.class) {
                    return ResponseEntity.status(406).body("请求勿重复请求");
                } else if (particle.getType() == TimesLimiter.class) {// 次数限制类型
                    return ResponseEntity.status(406).body("超过使用次数：" + particle.getValue() + "次");
                }
            }
            // 模拟业务处理耗时
            Thread.sleep(6000);

            return ResponseEntity.ok("发送成功，当前次数：" + particle.getValue());
        });
    }

    // 基于注解的拦截链组合方式
    @RequestMapping("verify2")
    @Limit(name = "user:send", key = "#phone", expire = 60L, limiterBeanClass = BarrierLimiter.class)
    public ResponseEntity<String> verify2(String phone, Particle particle/*这个状态值自动注入*/) throws Throwable {
        log.info("verify2: phone={}", phone);
        // 判断是否被限制
        if (particle.isLimited()) {
            // 如果是去重限制类型
            if (particle.getType() == IdempotentLimiter.class) {
                return ResponseEntity.status(406).body("请求勿重复请求");
            } else if (particle.getType() == TimesLimiter.class) {// 次数限制类型
                return ResponseEntity.status(406).body("超过使用次数：" + particle.getValue() + "次");
            }
        }

        // 模拟业务处理耗时
        Thread.sleep(5000);

        return ResponseEntity.ok("发送成功，当前次数：" + particle.getValue());
    }

    // 第三方一次请求多次调用限制（唯一约束：订单号+状态值）
    @RequestMapping("notify")
    @Limit(name = "notify:order", key = "#params['orderId']+'-'+#params['state']", expire = 60L, limiterBeanName = "idempotentLimiter")
    public void notify(@RequestBody Map<String, Object> params, Particle particle, HttpServletResponse resp) throws Exception {
        log.info("params: {}", params);
        if (particle.isLimited()) {
            resp.sendError(406, "请求勿重复调用");
            return;
        }

        // 模拟业务处理耗时
        Thread.sleep(5000);

        resp.setStatus(200);
        resp.getWriter().println("OK");
        resp.getWriter().flush();
    }

    @RequestMapping("pay")
    public String pay(String orderNo) throws InterruptedException {
        Thread.sleep(5000);
        return "OK";
    }
}
