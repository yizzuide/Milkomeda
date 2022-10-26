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
import com.github.yizzuide.milkomeda.ice.inspector.domain.JobInspectionDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MongoDB impl of job introspection information store.
 *
 * @author yizzuide
 * @since 3.14.0
 * <br>
 * Create at 2022/09/26 23:02
 */
public class MongoJobInspector extends AbstractJobInspector {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public long size() {
        Query query = new Query();
        return mongoTemplate.count(query, JobInspectionDocument.class);
    }

    @Override
    public JobWrapper get(String jobId) {
        JobInspectionDocument jobInspectionDocument = mongoTemplate.findById(Ice.getId(jobId), JobInspectionDocument.class);
        return convertFromEntity(jobInspectionDocument);
    }

    @Override
    public List<JobWrapper> getPage(int start, int size, int order) {
        // [-1, 1]
        order = Math.min(1, Math.max(-1, order));
        int pageCount = start * size;
        boolean useUpdate = IceHolder.getProps().getIntrospect().getIndexType() == IndexType.UPDATE_TIME;
        Pageable pageable = PageRequest.of(pageCount, size);
        Sort sort = Sort.by(order == -1 ? Sort.Direction.DESC : Sort.Direction.ASC, useUpdate ? "updateTime" : "pushTime");
        Query query = new Query();
        query.with(pageable).with(sort);
        List<JobInspectionDocument> jobInspectionCollection = mongoTemplate.find(query, JobInspectionDocument.class);
        return jobInspectionCollection.stream().map(this::convertFromEntity).collect(Collectors.toList());
    }

    @Override
    protected void doAdd(JobWrapper jobWrapper) {
        JobInspectionDocument jobInspectionDocument = convertToEntity(jobWrapper);
        mongoTemplate.save(jobInspectionDocument);
    }

    @Override
    protected void doUpdate(List<JobWrapper> jobWrappers) {
        jobWrappers.forEach(jobWrapper -> jobWrapper.setUpdateTime(System.currentTimeMillis()));
        jobWrappers.stream().map(this::convertToEntity)
                .forEach(jobInspectionDocument -> {
                    Query query = Query.query(Criteria.where("id").is(jobInspectionDocument.getId()));
                    Update update = Update.update("queueType", jobInspectionDocument.getQueueType())
                                    .set("bucketIndex", jobInspectionDocument.getBucketIndex())
                                    .set("hadRetryCount", jobInspectionDocument.getHadRetryCount())
                                    .set("needRePush", jobInspectionDocument.getNeedRePush())
                                    .set("executionTime", jobInspectionDocument.getExecutionTime())
                                    .set("updateTime", jobInspectionDocument.getUpdateTime());
                    mongoTemplate.upsert(query, update, JobInspectionDocument.class, JobInspectionDocument.COLLECTION_NAME);
                });
    }

    @Override
    public void finish(List<String> jobIds) {
        super.finish(jobIds);
        jobIds.forEach(jobId -> {
            Query query = Query.query(Criteria.where("id").is(Ice.getId(jobId)));
            mongoTemplate.remove(query, JobInspectionDocument.class);
        });
    }

    private JobInspectionDocument convertToEntity(JobWrapper jobWrapper) {
        JobInspectionDocument jobInspectionDocument = new JobInspectionDocument();
        jobInspectionDocument.setId(Ice.getId(jobWrapper.getId()));
        jobInspectionDocument.setTopic(jobWrapper.getTopic());
        jobInspectionDocument.setApplicationName(jobWrapper.getApplicationName());
        jobInspectionDocument.setQueueType(jobWrapper.getQueueType().ordinal());
        jobInspectionDocument.setBucketIndex(jobWrapper.getBucketIndex());
        jobInspectionDocument.setHadRetryCount(jobWrapper.getHadRetryCount());
        jobInspectionDocument.setExecutionTime(jobWrapper.getExecutionTime());
        jobInspectionDocument.setNeedRePush(jobWrapper.isNeedRePush() ? 1 : 0);
        jobInspectionDocument.setUpdateTime(jobWrapper.getUpdateTime());
        jobInspectionDocument.setPushTime(jobWrapper.getPushTime());
        return jobInspectionDocument;
    }

    private JobWrapper convertFromEntity(JobInspectionDocument jobInspectionDocument) {
        if (jobInspectionDocument == null) {
            return null;
        }
        JobWrapper JobWrapper = new JobWrapper();
        JobWrapper.setId(Ice.mergeId(jobInspectionDocument.getId(), jobInspectionDocument.getTopic()));
        JobWrapper.setTopic(jobInspectionDocument.getTopic());
        JobWrapper.setApplicationName(jobInspectionDocument.getApplicationName());
        JobWrapper.setQueueType(JobQueueType.values()[jobInspectionDocument.getQueueType()]);
        JobWrapper.setExecutionTime(jobInspectionDocument.getExecutionTime());
        JobWrapper.setBucketIndex(jobInspectionDocument.getBucketIndex());
        JobWrapper.setHadRetryCount(jobInspectionDocument.getHadRetryCount());
        JobWrapper.setNeedRePush(jobInspectionDocument.getNeedRePush() == 1);
        JobWrapper.setUpdateTime(jobInspectionDocument.getUpdateTime());
        JobWrapper.setPushTime(jobInspectionDocument.getPushTime());
        return JobWrapper;
    }
}
