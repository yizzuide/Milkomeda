package com.github.yizzuide.milkomeda.hydrogen.transaction;

import com.github.yizzuide.milkomeda.hydrogen.core.HydrogenProperties;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.interceptor.NameMatchTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;
import org.springframework.util.CollectionUtils;

import java.util.stream.Collectors;

/**
 * TransactionAdviceConfig
 * 切面事务配置
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2019/11/25 10:56
 */
@Aspect
@Configuration
@EnableConfigurationProperties(HydrogenProperties.class)
@AutoConfigureAfter(TransactionAutoConfiguration.class)
@ConditionalOnProperty(prefix = "milkomeda.hydrogen.transaction", name = "enable", havingValue = "true", matchIfMissing = true)
public class TransactionAdviceConfig {

    @Autowired
    private HydrogenProperties props;

    @Bean
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public TransactionInterceptor txAdvice(PlatformTransactionManager transactionManager) {
        RuleBasedTransactionAttribute txAttr_REQUIRED = new RuleBasedTransactionAttribute();
        // 设置传播行为：若当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。这是默认值。
        txAttr_REQUIRED.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        // 抛出异常后执行切点回滚
        txAttr_REQUIRED.setRollbackRules(props.getTransaction().getRollbackWhenException()
                .stream().map(RollbackRuleAttribute::new).collect(Collectors.toList()));
        // 设置超时
        txAttr_REQUIRED.setTimeout((int) props.getTransaction().getRollbackWhenTimeout().getSeconds());

        RuleBasedTransactionAttribute txAttr_READONLY = new RuleBasedTransactionAttribute();
        // 设置传播行为：以非事务运行，如果当前存在事务，则把当前事务挂起
        txAttr_READONLY.setPropagationBehavior(TransactionDefinition.PROPAGATION_NOT_SUPPORTED);
        txAttr_READONLY.setReadOnly(true);

        NameMatchTransactionAttributeSource source = new NameMatchTransactionAttributeSource();
        // 开启只读,提高数据库访问性能
        if (!CollectionUtils.isEmpty(props.getTransaction().getReadOnlyPrefix())) {
            for (String prefix : props.getTransaction().getReadOnlyPrefix()) {
                source.addTransactionalMethod(prefix, txAttr_READONLY);
            }
        }

        if (!CollectionUtils.isEmpty(props.getTransaction().getReadOnlyAppendPrefix())) {
            for (String prefix : props.getTransaction().getReadOnlyAppendPrefix()) {
                source.addTransactionalMethod(prefix, txAttr_READONLY);
            }
        }

        // 其它都需要事务
        source.addTransactionalMethod("*", txAttr_REQUIRED);
        return new TransactionInterceptor(transactionManager, source);
    }

    @Bean
    public Advisor txAdviceAdvisor(TransactionInterceptor txAdvice) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression(props.getTransaction().getPointcutExpression());
        return new DefaultPointcutAdvisor(pointcut, txAdvice);
    }
}
