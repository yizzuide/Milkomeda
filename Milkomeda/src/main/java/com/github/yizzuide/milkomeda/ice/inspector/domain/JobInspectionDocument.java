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

package com.github.yizzuide.milkomeda.ice.inspector.domain;

import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;

/**
 * Job inspection document for mongodb.
 *
 * @author yizzuide
 * @since 3.14.0
 * <br>
 * Create at 2022/09/26 23:14
 */
@Data
@Document(JobInspectionDocument.COLLECTION_NAME)
public class JobInspectionDocument implements Serializable {
    private static final long serialVersionUID = 7825090329714922254L;

    public static final String COLLECTION_NAME = "job_inspection";

    /**
     * Job id.
     */
    @MongoId
    private String id;

    /**
     * Job topic name.
     */
    @Field("topic")
    private String topic;

    /**
     * Which application does the job come from.
     */
    @Field("application_name")
    private String applicationName;

    /**
     * Job into what queue type.
     */
    @Field("queue_type")
    private Integer queueType;

    /**
     * Which bucket index does the job come from.
     */
    @Field("bucket_index")
    private Integer bucketIndex;

    /**
     * Number of retries already happened.
     */
    @Field("hadRetry_count")
    private Integer hadRetryCount;

    /**
     * The job has need to re-push.
     */
    @Field("need_re_push")
    private Integer needRePush;

    /**
     * Next execution time with job.
     */
    @Field(name = "execution_time", targetType = FieldType.TIMESTAMP)
    private Long executionTime;

    /**
     * Job push time.
     */
    // create index named 'push_time.index', and created in the background.
    @Indexed(name = "index", background = true)
    @Field(name = "push_time", targetType = FieldType.TIMESTAMP)
    private Long pushTime;

    /**
     * Latest update time executed.
     */
    // create index named 'update_time.index', and created in the background.
    @Indexed(name = "index", background = true)
    @Field(name = "update_time", targetType = FieldType.TIMESTAMP)
    private Long updateTime;
}
