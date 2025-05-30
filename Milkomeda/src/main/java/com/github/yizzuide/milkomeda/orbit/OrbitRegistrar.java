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
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Register orbit advisor.
 *
 * @see org.springframework.aop.support.AbstractExpressionPointcut
 * @see org.springframework.aop.aspectj.AspectJExpressionPointcutAdvisor
 * @see org.springframework.beans.factory.config.BeanPostProcessor
 * @since 3.13.0
 * @version 3.20.0
 * @author yizzuide
 * <br>
 * Create at 2022/02/21 01:14
 */
@Slf4j
public class OrbitRegistrar implements ImportBeanDefinitionRegistrar {
    // 切面提供者包扫描路径
    public static final String ORBIT_SOURCE_PROVIDER_SCAN_BASE_PACKAGES = "com.github.yizzuide.milkomeda.*.orbit";

    private final Environment environment;

    // 注入Environment
    OrbitRegistrar(Environment environment) {
        this.environment = environment;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        List<OrbitAdvisor> orbitAdvisors = new ArrayList<>();
        // 1.YAML配置方式
        OrbitProperties orbitProperties;
        BindResult<OrbitProperties> bindResult = Binder.get(this.environment).bind(OrbitProperties.PREFIX, OrbitProperties.class);
        if (bindResult.isBound()) {
            orbitProperties = bindResult.get();
        } else {
            // 没用配置过Orbit，创建默认配置
            orbitProperties = new OrbitProperties();
        }
        List<OrbitProperties.Item> orbitItems = orbitProperties.getInstances();
        if (!CollectionUtils.isEmpty(orbitItems)) {
            orbitItems.forEach(item -> {
                OrbitAdvisor orbitAdvisor = ReflectUtil.newInstance(item.getAdvisorClazz());
                if (orbitAdvisor != null) {
                    orbitAdvisor.initFrom(item);
                    orbitAdvisors.add(orbitAdvisor);
                }
            });
        }

        // 2.框架其它模块桥接切面源提供者
        Collection<OrbitSource> orbitSources = SpringContext.scanBeans(registry, OrbitSourceProvider.class, ORBIT_SOURCE_PROVIDER_SCAN_BASE_PACKAGES);
        orbitSources.forEach(orbitSource -> {
            List<OrbitAdvisor> advisors = orbitSource.createAdvisors(this.environment);
            if (!CollectionUtils.isEmpty(advisors)) {
                orbitAdvisors.addAll(advisors);
            }
        });

        // 3.注解注册方式
        ConfigurableListableBeanFactory beanFactory = (ConfigurableListableBeanFactory) registry;
        Map<String, Object> orbits = beanFactory.getBeansWithAnnotation(Orbit.class);
        orbits.forEach((id, value) -> {
            Class<OrbitAdvice> adviceClass = (Class<OrbitAdvice>) value.getClass();
            Orbit orbit = AnnotationUtils.findAnnotation(adviceClass, Orbit.class);
            if (orbit != null) {
                if (StringUtils.hasLength(orbit.pointcutExpression())) {
                    orbitAdvisors.add(new AspectJOrbitAdvisor(orbit.pointcutExpression(), id, adviceClass, null));
                    return;
                }
                if (orbit.pointcutAnnotation() != Annotation.class) {
                    orbitAdvisors.add(AnnotationOrbitAdvisor.forMethod(orbit.pointcutAnnotation(), id, adviceClass, null));
                }
            }
        });

        if (CollectionUtils.isEmpty(orbitAdvisors)) {
            return;
        }
        // register advisor
        // 默认的自动代理会添加配置的Advisor Bean: AnnotationAwareAspectJAutoProxyCreator.findCandidateAdvisors() ->
        //  AbstractAdvisorAutoProxyCreator.getAdvicesAndAdvisorsForBean() -> findEligibleAdvisors() ->
        //  findCandidateAdvisors() -> BeanFactoryAdvisorRetrievalHelper.findAdvisor()
        orbitAdvisors.forEach(orbitAdvisor ->
                registry.registerBeanDefinition("mk_orbit_advisor_" + orbitAdvisor.getAdvisorId(),
                        orbitAdvisor.createAdvisorBeanDefinition(registry)));
    }
}
