/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
 * <br />
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
