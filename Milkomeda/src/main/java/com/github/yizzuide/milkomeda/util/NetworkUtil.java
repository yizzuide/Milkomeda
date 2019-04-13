package com.github.yizzuide.milkomeda.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * NetworkUtil
 *
 * @author yizzuide
 * @since 0.2.0
 * Create at 2019/04/11 20:13
 */
public class NetworkUtil {
    /**
     * 获取服务器IP地址
     *
     * @return 服务器IP地址
     * @throws UnknownHostException 抛出网络异常
     */
    public static String getHost() throws UnknownHostException {
        InetAddress addr = InetAddress.getLocalHost();
        // 获取本机ip;
        return addr.getHostAddress();
    }
}
