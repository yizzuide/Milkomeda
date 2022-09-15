package com.github.yizzuide.milkomeda.demo.ice.controller;

import com.github.yizzuide.milkomeda.ice.Ice;
import com.github.yizzuide.milkomeda.ice.Job;
import com.github.yizzuide.milkomeda.ice.JobWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

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
    public void push(@RequestBody Job<?> job) {
        ice.add(job);
    }

    /**
     * 获取job页数据
     * @param start page num
     * @param size  size of pre page
     * @param order sorting of corresponding column, 1 is asc and -1 is desc
     * @return  JobWrapper list
     */
    @GetMapping("getPage")
    public List<JobWrapper> getJobPage(int start, int size, int order) {
        return ice.getJobInspectPage(start, size, order);
    }

    /**
     * 获取当前job的详情信息
     * @param jobId job id
     * @return  job
     */
    @GetMapping("getDetail")
    public Job<?> getJobDetail(String jobId) {
        return ice.getJobDetail(jobId);
    }

    /**
     * 获取缓存key
     * @param jobId job id
     * @return  key map
     */
    @GetMapping("getJobCacheKeys")
    public Map<String, String> getJobCacheKeys(String jobId) {
        return ice.getCacheKey(jobId);
    }

    /**
     * 重推Job
     * @param jobId job id
     * @return null
     */
    @GetMapping("rePush")
    public ResponseEntity<Void> rePushJob(String jobId) {
        ice.rePushJob(jobId);
        return ResponseEntity.ok(null);
    }

    /**
     * 轮询可消费Job
     * @param topic 消费Topic标识
     * @param count 获取最多消费个数
     * @return List
     */
    @RequestMapping("pop")
    public List<Job<Map<String, Object>>> pop(String topic, Integer count) {
        List<Job<Map<String, Object>>> jobs = Collections.emptyList();
        try {
            if (count == null || count < 2) {
                Job<Map<String, Object>> job = ice.pop(topic);
                if (job != null) {
                    jobs = Collections.singletonList(job);
                }
                return jobs;
            }
            jobs = ice.pop(topic, count);
            return jobs;
        } finally {
            // 标记完成，清除元数据
            if (!CollectionUtils.isEmpty(jobs)) {
                ice.finish(jobs);
            }
        }
    }
}
