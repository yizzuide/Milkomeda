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

import io.etcd.jetcd.Client;
import io.etcd.jetcd.Lease;
import io.etcd.jetcd.Lock;
import lombok.Data;

/**
 * Simple warp with Etcd client and addition info.
 *
 * @see 3.15.0
 * @author yizzuide
 * <br>
 * Create at 2023/04/29 01:50
 */
@Data
public class EtcdClientInfo {
    /**
     * Etcd client.
     */
    private Client client;

    /**
     * Etcd lock client.
     */
    private Lock lockClient;

    /**
     * Etcd lease client.
     */
    private Lease leaseClient;

    /**
     * Etcd root lock key.
     */
    private String rootLockNode;

    public EtcdClientInfo(Client client, String rootLockNode) {
        this.client = client;
        this.lockClient = client.getLockClient();
        this.leaseClient = client.getLeaseClient();
        this.rootLockNode = rootLockNode;
    }
}
