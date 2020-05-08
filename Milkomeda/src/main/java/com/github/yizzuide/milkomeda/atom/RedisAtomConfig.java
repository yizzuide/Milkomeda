package com.github.yizzuide.milkomeda.atom;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AtomConfig
 *
 * @author yizzuide
 * @since 3.3.0
 * @since 3.3.0
 * Create at 2020/04/30 15:13
 */
@Configuration
@AutoConfigureAfter(RedisAutoConfiguration.class)
@EnableConfigurationProperties({RedisProperties.class, AtomProperties.class})
@ConditionalOnProperty(prefix = "milkomeda.atom", name = "strategy", havingValue = "REDIS", matchIfMissing = true)
public class RedisAtomConfig {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.atom.redis", name = "use-cluster", havingValue = "false", matchIfMissing = true)
    public RedissonClient redissonClient() {
        Config config = new Config();
        String redisUrl = String.format("redis://%s:%d", redisProperties.getHost(), redisProperties.getPort());
        SingleServerConfig singleServerConfig = config.useSingleServer();
        singleServerConfig
                .setAddress(redisUrl)
                .setPassword(redisProperties.getPassword())
                .setDatabase(redisProperties.getDatabase());
        if (redisProperties.getTimeout() != null) {
            singleServerConfig.setTimeout((int) redisProperties.getTimeout().toMillis());
        }
        return Redisson.create(config);
    }

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.atom.redis", name = "use-cluster", havingValue = "true")
    public RedissonClient clusterRedissonClient() {
        Config config = new Config();
        ClusterServersConfig clusterServersConfig = config.useClusterServers();
        for (String node : redisProperties.getCluster().getNodes()) {
            clusterServersConfig.addNodeAddress(String.format("redis://%s", node));
        }
        clusterServersConfig.setPassword(redisProperties.getPassword());
        if (redisProperties.getTimeout() != null) {
            clusterServersConfig.setTimeout((int) redisProperties.getTimeout().toMillis());
        }
        return Redisson.create(config);
    }

    @Bean
    public Atom atom(RedissonClient redissonClient) {
        return new RedisAtom(redissonClient);
    }
}
