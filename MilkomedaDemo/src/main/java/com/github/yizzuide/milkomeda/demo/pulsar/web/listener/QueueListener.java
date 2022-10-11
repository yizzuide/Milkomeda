package com.github.yizzuide.milkomeda.demo.pulsar.web.listener;

import com.github.yizzuide.milkomeda.demo.pulsar.pojo.User;
import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * QueueListener
 *
 * 模拟消息队列侦听器
 *
 * @author yizzuide
 * <br>
 * Create at 2019/03/30 00:32
 */
@Component
public class QueueListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(@NonNull ContextRefreshedEvent contextRefreshedEvent) {
        // 模拟另一个线程从队列里取得DeferredResult后处理响应
        new Thread(() -> {
            while (true) {
                DeferredResult<Object> deferredResult = PulsarHolder.getPulsar().getDeferredResult("84735252737");
                if (null != deferredResult) {
                    // 模拟超时
                    /*try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }*/

                    User user = new User();
                    user.setId(1L);
                    user.setName("yiz");
                    deferredResult.setResult(user);
                }

                try {
                    // 延时处理
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
