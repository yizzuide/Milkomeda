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

import com.github.yizzuide.milkomeda.ice.Ice;
import com.github.yizzuide.milkomeda.ice.IceHolder;
import com.github.yizzuide.milkomeda.ice.inspector.domain.JobInspection;
import com.github.yizzuide.milkomeda.ice.inspector.mapper.JobInspectionMapper;
import com.github.yizzuide.milkomeda.util.Dates;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mysql impl of job introspection information store.
 *
 * @author yizzuide
 * @since 3.14.0
 * <br>
 * Create at 2022/09/25 16:12
 */
public class MysqlJobInspector extends AbstractJobInspector {

    @Resource
    private JobInspectionMapper jobInspectionMapper;

    @Override
    public JobWrapper get(String jobId) {
        JobInspection jobInspection = jobInspectionMapper.queryById(Long.valueOf(Ice.getId(jobId)));
        return convertFromEntity(jobInspection);
    }

    @Override
    public long size() {
        return jobInspectionMapper.count(null);
    }

    @Override
    public List<JobWrapper> getPage(int start, int size, int order) {
        // [-1, 1]
        order = Math.min(1, Math.max(-1, order));
        int pageCount = start * size;
        JobInspection condition = null;
        Pageable pageable = PageRequest.of(pageCount, size);
        boolean useUpdate = IceHolder.getProps().getIntrospect().getIndexType() == IndexType.UPDATE_TIME;
        List<JobInspection> jobInspections = jobInspectionMapper.queryAllByLimit(condition, pageable, useUpdate, order);
        if (CollectionUtils.isEmpty(jobInspections)) {
            return Collections.emptyList();
        }
        return jobInspections.stream().map(this::convertFromEntity).collect(Collectors.toList());
    }

    @Async
    @Override
    public void finish(List<String> jobIds) {
        jobIds.forEach(jobId -> jobInspectionMapper.deleteById(Long.valueOf(Ice.getId(jobId))));
    }

    @Override
    public void doAdd(JobWrapper jobWrapper) {
        JobInspection jobInspection = convertToEntity(jobWrapper);
        jobInspectionMapper.insert(jobInspection);
    }

    @Override
    protected void doUpdate(List<JobWrapper> jobWrappers) {
        jobWrappers.forEach(jobWrapper -> jobWrapper.setUpdateTime(System.currentTimeMillis()));
        List<JobInspection> jobInspectionList = jobWrappers.stream().map(this::convertToEntity).collect(Collectors.toList());
        jobInspectionMapper.insertOrUpdateBatch(jobInspectionList);
    }

    private JobInspection convertToEntity(JobWrapper jobWrapper) {
        JobInspection jobInspection = new JobInspection();
        jobInspection.setId(Long.valueOf(Ice.getId(jobWrapper.getId())));
        jobInspection.setTopic(jobWrapper.getTopic());
        jobInspection.setApplicationName(jobWrapper.getApplicationName());
        jobInspection.setQueueType(jobWrapper.getQueueType().ordinal());
        jobInspection.setBucketIndex(jobWrapper.getBucketIndex());
        jobInspection.setHadRetryCount(jobWrapper.getHadRetryCount());
        jobInspection.setExecutionTime(Dates.timestamp2Date(jobWrapper.getExecutionTime()));
        jobInspection.setNeedRePush(jobWrapper.isNeedRePush() ? 1 : 0);
        jobInspection.setUpdateTime(Dates.timestamp2Date(jobWrapper.getUpdateTime()));
        jobInspection.setPushTime(Dates.timestamp2Date(jobWrapper.getPushTime()));
        return jobInspection;
    }

    private JobWrapper convertFromEntity(JobInspection jobInspection) {
        if (jobInspection == null) {
            return null;
        }
        JobWrapper JobWrapper = new JobWrapper();
        JobWrapper.setId(Ice.mergeId(jobInspection.getId(), jobInspection.getTopic()));
        JobWrapper.setTopic(jobInspection.getTopic());
        JobWrapper.setApplicationName(jobInspection.getApplicationName());
        JobWrapper.setQueueType(JobQueueType.values()[jobInspection.getQueueType()]);
        JobWrapper.setBucketIndex(jobInspection.getBucketIndex());
        JobWrapper.setHadRetryCount(jobInspection.getHadRetryCount());
        JobWrapper.setExecutionTime(Dates.date2Timestamp(jobInspection.getExecutionTime()));
        JobWrapper.setNeedRePush(jobInspection.getNeedRePush() == 1);
        JobWrapper.setUpdateTime(Dates.date2Timestamp(jobInspection.getUpdateTime()));
        JobWrapper.setPushTime(Dates.date2Timestamp(jobInspection.getPushTime()));
        return JobWrapper;
    }
}
