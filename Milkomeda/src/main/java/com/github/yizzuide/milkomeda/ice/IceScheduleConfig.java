package com.github.yizzuide.milkomeda.ice;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.util.CollectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * IceScheduleConfig
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/17 17:00
 */
@Slf4j
@Configuration
@ConditionalOnMissingBean(SchedulingConfigurer.class)
@ConditionalOnProperty(prefix = "milkomeda.ice", name = "enable-task", havingValue = "true")
public class IceScheduleConfig implements SchedulingConfigurer {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private Ice ice;

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private IceProperties props;

    @SuppressWarnings("unchecked")
    public void configureTasks(ScheduledTaskRegistrar scheduledTaskRegistrar) {
        scheduledTaskRegistrar.setTaskScheduler(taskScheduler());
        Objects.requireNonNull(scheduledTaskRegistrar.getScheduler()).scheduleAtFixedRate(() -> {
            IceContext.getTopicMap().keySet().forEach(topic -> {
                List<Job<Map>> jobs = ice.pop(topic, props.getTaskTopicPopMaxSize());
                if (CollectionUtils.isEmpty(jobs)) return;
                List<IceExpress> iceExpressList = IceContext.getTopicMap().get(topic);
                for (IceExpress iceExpress : iceExpressList) {
                    try {
                        Method method = iceExpress.getMethod();
                        // 没有参数
                        if(method.getParameterTypes().length == 0) {
                            method.invoke(iceExpress.getTarget());
                            return;
                        }
                        Type[] genericParameterTypes = method.getGenericParameterTypes();
                        // 没有泛型参数
                        if (genericParameterTypes.length == 0) {
                            method.invoke(iceExpress.getTarget(), jobs);
                            return;
                        }
                        ParameterizedType parameterizedType = (ParameterizedType) genericParameterTypes[0];
                        Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                        // 子参数类型
                        Type[] subActualTypeArguments = ((ParameterizedTypeImpl) actualTypeArguments[0]).getActualTypeArguments();
                        if (subActualTypeArguments.length == 0) {
                            method.invoke(iceExpress.getTarget(), jobs);
                            return;
                        }
                        JavaType javaType = TypeFactory.defaultInstance().constructType(subActualTypeArguments[0]);
                        // 转换到业务类型
                        for (Job job : jobs) {
                            job.setBody(JSONUtil.nativeRead(JSONUtil.serialize(job.getBody()), javaType));
                        }
                        method.invoke(iceExpress.getTarget(), jobs);
                    } catch (Exception e) {
                        log.error("Ice schedule error: {}", e.getMessage(), e);
                        return;
                    }
                }
                ice.finish(jobs);
            });
        }, Duration.ofMillis(props.getTaskExecuteRate()));
    }

    @Bean("iceTaskExecutor")
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(props.getTaskPoolSize());
        executor.setThreadNamePrefix("ice-task-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 调度器shutdown被调用时等待当前被调度的任务完成
        executor.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时长
        executor.setAwaitTerminationSeconds(props.getTaskTerminationAwareSeconds());
        return executor;
    }
}
