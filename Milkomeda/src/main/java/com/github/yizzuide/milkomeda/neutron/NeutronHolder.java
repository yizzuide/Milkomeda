package com.github.yizzuide.milkomeda.neutron;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.boot.autoconfigure.quartz.QuartzProperties;

/**
 * NeutronHolder
 *
 * @author yizzuide
 * @since 1.18.0
 * @version 1.18.1
 * Create at 2019/12/09 21:21
 */
public class NeutronHolder {
    private static Scheduler scheduler;
    private static QuartzProperties props;

    public static void setScheduler(Scheduler scheduler) {
        NeutronHolder.scheduler = scheduler;
    }

    public static Scheduler getScheduler() throws SchedulerException {
        return scheduler;
    }

    public static void setProps(QuartzProperties props) {
        NeutronHolder.props = props;
    }

    public static QuartzProperties getProps() {
        return props;
    }
}
