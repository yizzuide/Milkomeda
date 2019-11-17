package com.github.yizzuide.milkomeda.ice;

import com.github.yizzuide.milkomeda.universe.context.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.*;

/**
 * IceContext
 *
 * @author yizzuide
 * @since 1.15.0
 * Create at 2019/11/17 18:40
 */
@Slf4j
public class IceContext implements ApplicationListener<ContextRefreshedEvent> {

    private static Map<String, List<IceExpress>> topicMap = new HashMap<>();

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        Map<String, Object> beanMap = ApplicationContextHolder.get().getBeansWithAnnotation(IceHandler.class);
        for (String key : beanMap.keySet()) {
            // 如果方法有aop切面，可以通过AopUtils.getTargetClass(beanMap.get(key).getClass())获取
            Method[] methods = ReflectionUtils.getAllDeclaredMethods(beanMap.get(key).getClass());
            for (Method method : methods) {
                // 获取指定方法上的注解的属性
                final IceListener iceListener = AnnotationUtils.findAnnotation(method, IceListener.class);
                if (null != iceListener) {
                    String topicName = StringUtils.isEmpty(iceListener.topic()) ? iceListener.value() : iceListener.topic();
                    if (StringUtils.isEmpty(topicName)) {
                        throw new IllegalArgumentException("Please specify the topic of @IceListener!");
                    }
                    if (topicMap.containsKey(topicName)) {
                        topicMap.get(topicName).add(new IceExpress(topicName, beanMap.get(key), method));
                    } else {
                        List<IceExpress> mList = new ArrayList<>();
                        mList.add(new IceExpress(topicName, beanMap.get(key), method));
                        topicMap.put(topicName, mList);
                    }
                    break;
                }
            }
        }
    }

    static Map<String, List<IceExpress>> getTopicMap() {
        return topicMap;
    }
}
