/*
 * Copyright (c) 2023 yizzuide All rights Reserved.
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

import com.google.protobuf.ByteString;
import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;


/**
 * Etcd config.
 *
 * @since 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/04/29 01:39
 */
@Configuration
@EnableConfigurationProperties(AtomProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.atom", name = "strategy", havingValue = "ETCD")
public class EtcdAtomConfig {

    @Autowired
    private AtomProperties atomProperties;

    @Bean
    public Atom atom() {
        return new EtcdAtom();
    }

    @Bean
    public EtcdClientInfo etcdClientInfo() {
        AtomProperties.Etcd etcd = atomProperties.getEtcd();
        String[] endpoints = CollectionUtils.isEmpty(etcd.getEndpointUrls()) ? new String[]{etcd.getEndpointUrl()} :
                etcd.getEndpointUrls().toArray(new String[]{});
        Client client;
        if (etcd.getUser() == null) {
            client = Client.builder()
                    .endpoints(endpoints)
                    .connectTimeout(etcd.getConnectTimeout())
                    .keepaliveTime(etcd.getKeepaliveTime())
                    .keepaliveTimeout(etcd.getKeepaliveTime())
                    .build();
        } else {
            client = Client.builder()
                    .endpoints(endpoints)
                    .user(ByteSequence.from((ByteString.copyFromUtf8(etcd.getUser()))))
                    .password(ByteSequence.from((ByteString.copyFromUtf8(etcd.getPassword()))))
                    .authority(etcd.getAuthority())
                    .connectTimeout(etcd.getConnectTimeout())
                    .keepaliveTime(etcd.getKeepaliveTime())
                    .keepaliveTimeout(etcd.getKeepaliveTime())
                    .build();
        }
        return new EtcdClientInfo(client, etcd.getRootLockNode());
    }
}
