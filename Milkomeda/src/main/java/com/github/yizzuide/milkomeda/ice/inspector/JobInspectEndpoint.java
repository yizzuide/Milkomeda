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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpoint;

import java.util.HashMap;
import java.util.Map;

/**
 * Job inspection info exposed over HTTP.
 *
 * @since 3.14.0
 * @author yizzuide
 * <br>
 * Create at 2022/10/12 15:37
 */
// Spring boot 2.x 提供了@ControllerEndpoint和@RestControllerEndpoint实现仅由 Spring MVC 或 Spring WebFlux 公开的端点
//@RestControllerEndpoint(id = "ice-job")
// @WebEndpoint exposed only over HTTP and not over JMX.
@WebEndpoint(id = "ice-job")
//@Endpoint
public class JobInspectEndpoint {

    @Autowired
    private Ice ice;

    // GET http://host:port/actuator/ice-job?topic=xxx&jobId=xxx
    @ReadOperation
    public Map<String, Object> info(String topic, String jobId) {
        Map<String, Object> result = new HashMap<>();
        JobWrapper jobInspectInfo = ice.getJobInspectInfo(topic, jobId);
        if (jobInspectInfo == null) {
            result.put("job", null);
            result.put("reason", "Not found, may not exist or has consumed by calling pull.");
            return result;
        }
        result.put("job", new HashMap<String, Object>(){
            private static final long serialVersionUID = -8977272265465849907L;
            {
                put("jobInspectInfo", jobInspectInfo);
                put("jobDetail", ice.getJobDetail(jobId, topic));
                put("cacheKeys", ice.getCacheKey(jobId, topic));
        }});
        return result;
    }

    // POST http://host:port/actuator/ice-job
    // body {"topic": "check_order","jobId": "100112345"}
    @WriteOperation
    public Map<String, Object> push(String topic, String jobId) {
        JobWrapper jobInspectInfo = ice.getJobInspectInfo(topic, jobId);
        Map<String, Object> result = new HashMap<>();
        boolean pushed = false;
        if (jobInspectInfo.isNeedRePush()) {
            pushed = ice.rePushJob(topic, jobId);
        }
        result.put("isPushed", pushed);
        if (!pushed) {
            result.put("reason", "Not need to push at current state type[" + jobInspectInfo.getQueueType() +"].");
        }
        return result;
    }

    // DELETE http://host:port/actuator/ice-job?topic=xxx&jobId=xxx
    @DeleteOperation
    public Map<String, Object> remove(String topic, String jobId) {
        JobWrapper jobInspectInfo = ice.getJobInspectInfo(topic, jobId);
        Map<String, Object> result = new HashMap<>();
        boolean removed = false;
        if (jobInspectInfo != null && ice.getJobDetail(jobId, topic) == null) {
            ice.finish(Ice.mergeId(jobId, topic));
            removed = true;
        }
        result.put("isRemoved", removed);
        if (!removed) {
            result.put("reason", "The Job is working well now.");
        }
        return result;
    }
}
