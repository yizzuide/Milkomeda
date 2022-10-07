package com.github.yizzuide.milkomeda.demo.neutron;


import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

/**
 * NeutronJob
 *
 * @author yizzuide
 * <br />
 * Create at 2019/12/09 23:45
 */
@Slf4j
@Component
//@DisallowConcurrentExecution // 不允许并发执行
public class NeutronJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("The neutron star rotates once");
    }
}
