package com.github.yizzuide.milkomeda.demo.ice.controller;

import com.github.yizzuide.milkomeda.ice.Ice;
import com.github.yizzuide.milkomeda.ice.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * JobController
 *
 * @author yizzuide
 * Create at 2019/11/18 20:30
 */
@RestController
@RequestMapping("job")
public class JobController {

    @Autowired
    private Ice ice;

    /**
     * 延迟一个Job
     * @param job 必需设置属性：id（唯一id), topic（消费Topic标识）, delay（延迟ms), ttr（消费执行超时ms）, retryCount（重试次数）, body（JSON业务数据）
     */
    @RequestMapping("push")
    public void push(@RequestBody Job job) {
        ice.add(job);
    }

    /**
     * 轮询可消费Job
     * @param topic 消费Topic标识
     * @param count 获取最多消费个数
     * @return List
     */
    @RequestMapping("pop")
    public List<Job<Map>> pop(String topic, Integer count) {
        if (count == null) {
            return Collections.singletonList(ice.pop(topic));
        }
        return ice.pop(topic, count);
    }
}
