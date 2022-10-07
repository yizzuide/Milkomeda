package com.github.yizzuide.milkomeda.demo.pulsar.web.controller;

import com.github.yizzuide.milkomeda.demo.pulsar.pojo.User;
import com.github.yizzuide.milkomeda.pulsar.PulsarDeferredResult;
import com.github.yizzuide.milkomeda.pulsar.PulsarFlow;
import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * UserController
 *
 * @author yizzuide
 * <br />
 * Create at 2019/03/30 19:17
 */
@Slf4j
@RestController
@RequestMapping("user")
public class UserController {

    // 使用默认的 WebAsyncTask 方式（这种方式一般没多大用处，使用默认tomcat线程处理就行）
    @PulsarFlow
    @GetMapping("login")
    public Object /* 这里的返回值必需为Object */ login(String username) throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName(username);
        // 查看当前线程：pulsar-1
        log.info("login user: {}", username);

        // 模拟耗时处理
//        Thread.sleep(2000);

        // 测试运行时异常
//        throw new RuntimeException("用户不存在");
        // 测试普通异常
//        throw new IOException("file not find");
        return user;
    }


    // DeferredResult方式需要注入PulsarDeferredResult
    @PulsarFlow(useDeferredResult = true)
    @GetMapping("{id:\\d+}")
    public Object /* 这里的返回值必需为Object */ getInfo(@PathVariable("id") Long id, PulsarDeferredResult pulsarDeferredResult) throws Exception {
        // 当前线程main
        log.info("findById：" + id);

        // 设置当前DeferredResult唯一标识，用于另一线程（在QueueListener类）获取
        pulsarDeferredResult.setDeferredResultID(/*RandomStringUtils.random(8)*/"84735252737");

        // 测试运行时异常
//        throw new RuntimeException("用户不存在");
        // 测试普通异常
//        throw new IOException();

        // 实际结果由DeferredResult返回，这里返回null即可
        return null;
    }

    // 支持EL表达式设置DeferredResultID
    @PulsarFlow(useDeferredResult = true, id = "#id")
    @GetMapping("notice/{id:\\d+}")
    public Object /* 这里的返回值必需为Object */ sendNotice(@PathVariable("id") Long id) {
        log.info("sendNotice：" + id);
        log.info("pulsarHolder: {}", PulsarHolder.getPulsar());

        // 直接返回响应
//        return ResponseEntity.ok("OK");

        // 延迟返回响应方式
        return PulsarHolder.defer(() -> ResponseEntity.ok("OK"), id);
    }
}
