package com.github.yizzuide.milkomeda.comet.collector;

import com.github.yizzuide.milkomeda.comet.core.CometAspect;
import com.github.yizzuide.milkomeda.comet.core.CometConfig;
import com.github.yizzuide.milkomeda.comet.core.CometData;
import com.github.yizzuide.milkomeda.comet.core.CometHolder;
import com.github.yizzuide.milkomeda.pillar.PillarExecutor;
import com.github.yizzuide.milkomeda.universe.polyfill.SpringMvcPolyfill;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * CometCollectorConfig
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.3.0
 * @see BeanFactoryUtils#beansOfTypeIncludingAncestors(org.springframework.beans.factory.ListableBeanFactory, java.lang.Class)
 * Create at 2020/03/28 18:54
 */
@Configuration
@AutoConfigureAfter(CometConfig.class)
@EnableConfigurationProperties(CometCollectorProperties.class)
@ConditionalOnProperty(prefix = "milkomeda.comet.collector", name = "enable", havingValue = "true")
public class CometCollectorConfig implements ApplicationContextAware {

    @Autowired
    private CometCollectorProperties props;

    @Autowired(required = false)
    private List<Collector> collectors;

    @Bean
    public CollectorFactory collectorFactory() {
        PillarExecutor<CometData, Object> pillarExecutor = new PillarExecutor<>();
        if (!CollectionUtils.isEmpty(collectors)) {
            pillarExecutor.addPillarList(collectors.stream().filter(c -> StringUtils.hasLength(c.supportType())).collect(Collectors.toList()));
        }
        return new CollectorFactory(pillarExecutor);
    }

    @Bean
    public CollectorRecorder collectorRecorder() {
        return new CollectorRecorder(collectorFactory());
    }

    @SuppressWarnings("all")
    @Autowired
    public void config(CometAspect cometAspect, CometCollectorProperties cometCollectorProperties) {
        // 设置日志采集器
        cometAspect.setRecorder(collectorRecorder());
        CometHolder.setCollectorProps(cometCollectorProperties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "milkomeda.comet.collector", name = "enable-tag", havingValue = "true")
    public CometCollectorResponseBodyAdvice cometResponseBodyAdvice() {
        return new CometCollectorResponseBodyAdvice();
    }

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public void configResponseBodyAdvice(RequestMappingHandlerAdapter adapter, HandlerExceptionResolver handlerExceptionResolver) {
        if (!props.isEnableTag()) {
            return;
        }
        CometCollectorResponseBodyAdvice collectorResponseBodyAdvice = cometResponseBodyAdvice();
        // 动态添加到响应处理
        SpringMvcPolyfill.addDynamicResponseBodyAdvice(adapter.getReturnValueHandlers(), handlerExceptionResolver, collectorResponseBodyAdvice);
    }

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        // 如果为空，到BeanFactory里查找
        if (CollectionUtils.isEmpty(collectors)) {
            collectors = new ArrayList<>(BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext, Collector.class).values());
            if (CollectionUtils.isEmpty(collectors)) {
                return;
            }
            applicationContext.getBean(CollectorFactory.class).getPillarExecutor().addPillarList(collectors.stream().filter(c -> StringUtils.hasLength(c.supportType())).collect(Collectors.toList()));
        }
    }
}
