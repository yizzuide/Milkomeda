package com.github.yizzuide.milkomeda.atom;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;

/**
 * ZkAtomConfig
 *
 * @author yizzuide
 * @since 3.3.0
 * Create at 2020/05/01 12:27
 */
@Configuration
@EnableConfigurationProperties(AtomProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.atom", name = "strategy", havingValue = "ZK")
public class ZkAtomConfig implements ApplicationContextAware {

    @Autowired
    private AtomProperties props;

    @Bean
    public Atom atom() {
        return new ZkAtom();
    }

    @Bean
    public CuratorFramework zkClient() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry((int) props.getZk().getSleepTime().toMillis(), props.getZk().getMaxRetry());
        return CuratorFrameworkFactory.newClient(props.getZk().getAddress(), retryPolicy);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        CuratorFramework zkClient = applicationContext.getBean(CuratorFramework.class);
        zkClient.start();
    }
}
