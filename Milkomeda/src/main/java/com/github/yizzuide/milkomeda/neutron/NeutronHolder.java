package com.github.yizzuide.milkomeda.neutron;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * NeutronHolder
 *
 * @author yizzuide
 * @since 1.18.0
 * Create at 2019/12/09 21:21
 */
public class NeutronHolder {
    private static Scheduler scheduler;
    private static NeutronProperties props;

    public static void setScheduler(Scheduler scheduler) {
        NeutronHolder.scheduler = scheduler;
    }

    public static Scheduler getScheduler() throws SchedulerException {
        return scheduler;
    }

    public static void setProps(NeutronProperties props) {
        NeutronHolder.props = props;
    }

    public static NeutronProperties getProps() {
        return props;
    }
}
