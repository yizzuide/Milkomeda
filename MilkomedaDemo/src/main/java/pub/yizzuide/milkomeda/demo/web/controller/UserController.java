package pub.yizzuide.milkomeda.demo.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import pub.yizzuide.milkomeda.demo.pojo.User;
import yiz.milkomeda.pulsar.Pulsar;
import yiz.milkomeda.pulsar.PulsarAsync;
import yiz.milkomeda.pulsar.PulsarDeferredResult;

import java.util.UUID;

/**
 * UserController
 *
 * @author yizzuide
 * Create at 2019/03/30 19:17
 */
@Slf4j
@RestController
@RequestMapping("user")
public class UserController {
    // 使用默认的 WebAsyncTask 方式（实际应用场景可用于上传文件处理）
    @PulsarAsync
    @GetMapping("login")
    public Object /* TODO 这里的返回值必需为Object */ login(String username) throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName(username);
        // 查看当前线程：pulsar-1
        log.info("login user: {}", username);

        // 模拟耗时处理
//        Thread.sleep(2000);

        // 测试运行时异常
//        throw new YizException(user.getId(), "user", "用户不存在");
        // 测试普通异常
//        throw new IOException("file not find");
        return user;
    }

    // 使用灵活的DeferredResult异步处理，需要注入PulsarDeferredResult
    @PulsarAsync(useDeferredResult = true)
    @GetMapping("{id:\\d+}")
    public Object /* TODO 这里的返回值必需为Object */ getInfo(@PathVariable("id") Long id, PulsarDeferredResult pulsarDeferredResult) throws Exception {
        log.info("findById：" + id);

        // 设置当前DeferredResult唯一标识，用于另一线程（在QueueListener类）获取
        pulsarDeferredResult.setDeferredResultID(/*RandomStringUtils.random(8)*/"84735252737");

        // 测试运行时异常
//        throw new YizException(id, "user", "用户不存在");
        // 测试普通异常
//        throw new IOException();

        // 实际结果由DeferredResult返回，这里返回null即可
        return null;
    }


    // 注入Pulsar
    @Autowired
    private Pulsar pulsar;

    // 使用DeferredResult异步处理在同一方法使用的例子
    @PulsarAsync(useDeferredResult = true)
    @GetMapping("notice/{id:\\d+}")
    public Object /* TODO 这里的返回值必需为Object */ sendNotice(@PathVariable("id") Long id,
                                                        PulsarDeferredResult pulsarDeferredResult) {
        log.info("sendNotice：" + id);
        String deferredResultID = UUID.randomUUID().toString();
        pulsarDeferredResult.setDeferredResultID(deferredResultID);

        // 模拟当前方法中调用耗时数业务处理
        new Thread(() -> {
            log.info("execute business");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            DeferredResult<Object> deferredResult = pulsar.takeDeferredResult(deferredResultID);
            deferredResult.setResult("send OK");
        }).start();
        return null;
    }
}
