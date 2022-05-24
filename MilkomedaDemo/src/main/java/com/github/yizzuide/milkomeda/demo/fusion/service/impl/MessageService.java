package com.github.yizzuide.milkomeda.demo.fusion.service.impl;

import com.github.yizzuide.milkomeda.fusion.Fusion;
import org.springframework.stereotype.Service;

/**
 * MessageService
 *
 * @author yizzuide
 * Create at 2022/05/25 02:34
 */
// 该类所有被调用的方法不会执行，allowed支持Spring EL表达式
@Fusion(allowed = "false")
@Service
public class MessageService {
    public void send(String text) {
        System.out.println("发送短信，产品：" + text);
    }
}
