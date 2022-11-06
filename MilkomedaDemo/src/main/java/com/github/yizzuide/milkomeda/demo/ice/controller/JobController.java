package com.github.yizzuide.milkomeda.demo.ice.controller;

import com.github.yizzuide.milkomeda.hydrogen.uniform.ResultVO;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformPage;
import com.github.yizzuide.milkomeda.hydrogen.uniform.UniformResult;
import com.github.yizzuide.milkomeda.ice.Ice;
import com.github.yizzuide.milkomeda.ice.Job;
import com.github.yizzuide.milkomeda.ice.inspector.JobStatInfo;
import com.github.yizzuide.milkomeda.ice.inspector.JobWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * JobController
 *
 * @author yizzuide
 * <br>
 * Create at 2019/11/18 20:30
 */
@RestController
@RequestMapping("job")
public class JobController {

    @Autowired
    private Ice ice;

    /**
     * 添加Job延迟任务
     * @param job 必需设置属性：id（唯一id), topic（消费Topic标识）, delay（延迟ms), ttr（消费执行超时ms）, retryCount（重试次数）, body（JSON业务数据）
     */
    @RequestMapping("push")
    public void push(@RequestBody Job<?> job) {
        ice.add(job);
    }

    /**
     * 获取job页数据
     * @param start page num, begin at 1
     * @param size  size of pre page
     * @param order sorting of corresponding column, 1 is asc and -1 is desc
     * @return  job Inspection page data
     */
    @GetMapping("getPage")
    public ResultVO<UniformPage<JobWrapper>> getJobPage(Integer start, Integer size, Integer order) {
        return UniformResult.ok(ice.getJobInspectPage(start, size, order));
    }

    /**
     * 获取当前job的详情信息
     * @param jobId job id
     * @param topic job topic
     * @return  job
     */
    @GetMapping("getDetail")
    public Job<?> getJobDetail(String jobId, String topic) {
        return ice.getJobDetail(jobId, topic);
    }

    /**
     * 获取缓存key
     * @param jobId job id
     * @param topic job topic
     * @return  cache key map
     */
    @GetMapping("getJobCacheKeys")
    public Map<String, String> getJobCacheKeys(String jobId, String topic) {
        return ice.getCacheKey(jobId, topic);
    }

    /**
     * 重推Job
     * @param jobId job id
     * @param topic job topic
     * @return null
     */
    @GetMapping("rePush")
    public ResponseEntity<Void> rePushJob(String jobId, String topic) {
        ice.rePushJob(jobId, topic);
        return ResponseEntity.ok(null);
    }

    /**
     * 获取job统计数据
     * @return JobStatInfo
     */
    @GetMapping("getJobStat")
    public ResponseEntity<JobStatInfo> getJobStat() {
        return ResponseEntity.ok(ice.getStatInfo());
    }

    /**
     * 拉取可消费Job
     * @param topic 消费Topic标识
     * @param count 获取最多消费个数
     * @return job list
     */
    @RequestMapping("pull")
    public List<Job<Map<String, Object>>> pull(String topic, Integer count) {
        return ice.pull(topic, count);
    }
}
