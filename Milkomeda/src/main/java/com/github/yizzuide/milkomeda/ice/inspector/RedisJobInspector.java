/*
 * Copyright (c) 2022 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.ice.inspector;

import com.github.yizzuide.milkomeda.ice.IceInstanceChangeEvent;
import com.github.yizzuide.milkomeda.ice.IceKeys;
import com.github.yizzuide.milkomeda.ice.IceProperties;
import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.Strings;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationListener;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Redis impl of job introspection information cache.
 *
 * @author yizzuide
 * @since 3.14.0
 * <br>
 * Create at 2022/09/14 16:58
 */
public class RedisJobInspector extends AbstractJobInspector implements InitializingBean, ApplicationListener<IceInstanceChangeEvent> {

    private final IceProperties props;

    private StringRedisTemplate redisTemplate;

    private String jobInspectorCursorKey = IceKeys.JOB_INSPECTOR_CURSOR_KEY_PREFIX;
    private String jobInspectorDataKey = IceKeys.JOB_INSPECTOR_DATA_KEY_PREFIX;

    public RedisJobInspector(IceProperties props) {
        this.props = props;
        if (!IceProperties.DEFAULT_INSTANCE_NAME.equals(props.getInstanceName())) {
            this.jobInspectorCursorKey = IceKeys.JOB_INSPECTOR_CURSOR_KEY_PREFIX + ":" + props.getInstanceName();
            this.jobInspectorDataKey = IceKeys.JOB_INSPECTOR_DATA_KEY_PREFIX + ":" + props.getInstanceName();
        }
    }

    @Override
    public long size() {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(jobInspectorDataKey);
        Long size = ops.size();
        return size == null ? 0 : size;
    }

    public JobWrapper get(String jobId) {
        BoundHashOperations<String, String, String> ops = redisTemplate.boundHashOps(jobInspectorDataKey);
        String jsonObject = ops.get(jobId);
        if (Strings.isEmpty(jsonObject)) {
            return null;
        }
        return JSONUtil.parse(jsonObject, JobWrapper.class);
    }

    public List<JobWrapper> getPage(int start, int size, int order) {
        // [-1, 1]
        order = Math.min(1, Math.max(-1, order));
        // 累计页数量
        int pageNumTotalSize = (start - 1) * size;
        BoundZSetOperations<String, String> ops = redisTemplate.boundZSetOps(jobInspectorCursorKey);
        Set<String> jobIds = order < 0 ? ops.reverseRange(pageNumTotalSize, pageNumTotalSize + size) : ops.range(pageNumTotalSize, pageNumTotalSize + size);
        if (CollectionUtils.isEmpty(jobIds)) {
            return Collections.emptyList();
        }
        return jobIds.stream().map(this::get).collect(Collectors.toList());
    }

    @Async
    @Override
    public void finish(List<String> jobIds) {
        super.finish(jobIds);
        redisTemplate.boundZSetOps(jobInspectorCursorKey).remove(jobIds.toArray());
        redisTemplate.boundHashOps(jobInspectorDataKey).delete(jobIds.toArray());
    }

    @Override
    public void doAdd(JobWrapper jobWrapper) {
        long indexTime = this.props.getIntrospect().getIndexType() == IndexType.UPDATE_TIME ?
                jobWrapper.getUpdateTime() : jobWrapper.getPushTime();
        redisTemplate.boundZSetOps(jobInspectorCursorKey).add(jobWrapper.getId(), indexTime);
        redisTemplate.boundHashOps(jobInspectorDataKey).put(jobWrapper.getId(), JSONUtil.serialize(jobWrapper));
    }

    @Override
    protected void doUpdate(List<JobWrapper> jobWrappers) {
        jobWrappers.forEach(jobWrapper -> this.add(jobWrapper, true));
    }

    @Override
    public void afterPropertiesSet() {
        redisTemplate = ApplicationContextHolder.get().getBean(StringRedisTemplate.class);
    }

    @Override
    public void onApplicationEvent(IceInstanceChangeEvent event) {
        String instanceName = event.getSource().toString();
        this.jobInspectorCursorKey = IceKeys.JOB_INSPECTOR_CURSOR_KEY_PREFIX + ":" + instanceName;
        this.jobInspectorDataKey = IceKeys.JOB_INSPECTOR_DATA_KEY_PREFIX + ":" + instanceName;
    }
}
