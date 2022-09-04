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

package com.github.yizzuide.milkomeda.orbit;

import com.github.yizzuide.milkomeda.universe.context.SpringContext;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.aop.Advice;
import org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * OrbitRegistrar
 * 环绕切面注册
 * <br><br>
 * ImportBeanDefinitionRegistrar是Bean定义阶段注册器，适用于动态注册一个AspectJExpressionPointcutAdvisor <br>
 * BeanPostProcessor是Bean的创建完成后置处理器，然后给它包装一个代理，但这里不适用
 *
 * @author yizzuide
 * @since 3.13.0
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor
 * @see org.springframework.aop.support.AbstractExpressionPointcut
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * Create at 2022/02/21 01:14
 */
@Slf4j
public class OrbitRegistrar implements ImportBeanDefinitionRegistrar {
    // 切面提供者包扫描路径
    public static final String ORBIT_SOURCE_PROVIDER_SCAN_BASE_PACKAGES = "com.github.yizzuide.milkomeda.*.orbit";

    private OrbitProperties orbitProperties;

    private final Environment environment;

    // Spring会自动传入Environment参数，调用这个构造器
    OrbitRegistrar(Environment environment) {
        this.environment = environment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        // 切面配置绑定
        try {
            // YAML配置方式
            this.orbitProperties = Binder.get(this.environment).bind(OrbitProperties.PREFIX, OrbitProperties.class).get();
        } catch (Exception e) {
            // 没用配置过Orbit，创建默认配置
            this.orbitProperties = new OrbitProperties();
        }

        // 框架其它模块桥接切面源提供者
        Collection<OrbitSource> orbitSources = SpringContext.scanBeans(registry, OrbitSourceProvider.class, ORBIT_SOURCE_PROVIDER_SCAN_BASE_PACKAGES);
        orbitSources.forEach(orbitSource -> orbitSource.createNodes(this.environment).forEach(this::addNode));

        // 注解注册方式
        ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) registry;
        Map<String, Object> orbits = beanFactory.getBeansWithAnnotation(Orbit.class);
        orbits.forEach((id, value) -> {
            Class<OrbitAdvice> adviceClass = (Class<OrbitAdvice>) value.getClass();
            Orbit orbit = AnnotationUtils.findAnnotation(adviceClass, Orbit.class);
            assert orbit != null;
            String pointcutExpression = orbit.pointcutExpression();
            OrbitNode orbitNode = OrbitNode.builder().id(id).adviceClass(adviceClass).pointcutExpression(pointcutExpression).build();
            this.addNode(orbitNode);
        });

        // 注册切面
        List<OrbitProperties.Item> instances = this.orbitProperties.getInstances();
        if (CollectionUtils.isEmpty(instances)) {
            return;
        }
        for (OrbitProperties.Item item : instances) {
            // 生成advice的beanName
            // AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(item.getAdviceClassName()).getBeanDefinition();
            // String adviceBeanName = AnnotationBeanNameGenerator.INSTANCE.generateBeanName(beanDefinition, registry);

            // 使用ObjectProvider的延迟查找代替getBean()的立即查找，因为getBean()找不到会有异常
            ObjectProvider<OrbitAdvice> adviceProvider = (ObjectProvider<OrbitAdvice>) beanFactory.getBeanProvider(item.getAdviceClassName());
            // 只有XML配置方式需要手动创建实例
            Advice advice = adviceProvider.getIfAvailable(() -> ReflectUtil.newInstance(item.getAdviceClassName()));
            // set custom props
            if (item.getProps() != null) {
                ReflectUtil.setField(advice, item.getProps());
            }
            String beanName = "mk_orbit_advisor_" + item.getKeyName();
            BeanDefinition aspectJBean = BeanDefinitionBuilder.genericBeanDefinition(AspectJExpressionPointcutAdvisor.class)
                    .addPropertyValue("location", String.format("$$%s##", item.getKeyName())) // Set the location for debugging.
                    .addPropertyValue("expression", item.getPointcutExpression())
                    .addPropertyValue("advice", advice)
                    .getBeanDefinition();
            registry.registerBeanDefinition(beanName, aspectJBean);
        }
    }

    /**
     * 添加切面节点
     * @param orbitNode OrbitNode
     */
    private void addNode(OrbitNode orbitNode) {
        OrbitProperties.Item item = new OrbitProperties.Item();
        item.setKeyName(orbitNode.getId());
        item.setPointcutExpression(orbitNode.getPointcutExpression());
        item.setAdviceClassName(orbitNode.getAdviceClass());
        item.setProps(orbitNode.getProps());
        this.orbitProperties.getInstances().add(item);
    }
}
