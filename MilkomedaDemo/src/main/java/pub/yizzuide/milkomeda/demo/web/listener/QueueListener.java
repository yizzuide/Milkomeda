package pub.yizzuide.milkomeda.demo.web.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;
import pub.yizzuide.milkomeda.demo.pojo.User;
import pub.yizzuide.milkomeda.pulsar.Pulsar;

/**
 * QueueListener
 *
 * 模拟消息队列侦听器
 *
 * @author yizzuide
 * Create at 2019/03/30 00:32
 */
@Component
public class QueueListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private Pulsar pulsar;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        // 模拟另一个线程从队列里取得DeferredResult后处理响应
        new Thread(() -> {
            while (true) {
                DeferredResult<Object> deferredResult = pulsar.takeDeferredResult("84735252737");
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
