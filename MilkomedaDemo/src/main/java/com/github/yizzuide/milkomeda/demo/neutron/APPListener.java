package com.github.yizzuide.milkomeda.demo.neutron;

import com.github.yizzuide.milkomeda.neutron.Neutron;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * APPListener
 *
 * @author yizzuide
 * Create at 2019/12/09 23:48
 */
@Component
public class APPListener implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Neutron.addJob("neutron_rotation", NeutronJob.class, "1/5 * * * * ?");
    }
}
