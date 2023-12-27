/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.metadata.HandlerMetaData;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.task.TaskSchedulingAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * IceScheduleConfig
 *
 * @author yizzuide
 * @since 1.15.0
 * @version 4.0.0
 * <br>
 * Create at 2019/11/17 17:00
 */
@Slf4j
@Configuration
@AutoConfigureAfter(TaskSchedulingAutoConfiguration.class)
@ConditionalOnProperty(prefix = "milkomeda.ice", name = "enable-task", havingValue = "true")
public class IceScheduleConfig {

    // Spring Boot 3.0：TaskScheduler超级接口支持虚拟线程和传统线程池
    @Autowired
    @SuppressWarnings({"unchecked", "SpringJavaInjectionPointsAutowiringInspection", "rawtypes"})
    public void config(Ice ice, IceProperties props, TaskScheduler taskScheduler) {
        taskScheduler.scheduleAtFixedRate(() -> IceContext.getTopicMap().keySet().forEach(topic -> {
            List<Job<Map<String, Object>>> jobs = ice.pop(topic, props.getTaskTopicPopMaxSize());
            if (CollectionUtils.isEmpty(jobs)) return;

            List<HandlerMetaData> metaDataList = IceContext.getTopicMap().get(topic);
            Object resultData = null;
            try {
                for (HandlerMetaData metaData : metaDataList) {
                    Method method = metaData.getMethod();
                    List<Job> jobList = (List) jobs;
                    Object result = ReflectUtil.invokeWithWrapperInject(metaData.getTarget(), method, jobList, Job.class, Job::getBody, Job::setBody);
                    if (result != null) {
                        resultData = result;
                    }
                }

                // 标记完成，清除元数据
                ice.finish(jobs);

                // 是否有重新入队
                if (resultData == null) {
                    return;
                }
                if (resultData instanceof Job) {
                    ice.add((Job) resultData);
                    return;
                }
                if (resultData instanceof List) {
                    List<Job> rePushJobs = (List<Job>) resultData;
                    for (Job rePushJob : rePushJobs) {
                        ice.add(rePushJob);
                    }
                }
            } catch (Exception e) {
                log.error("Ice schedule error: {}", e.getMessage(), e);
            }
        }), props.getTaskExecuteRate());
    }
}
