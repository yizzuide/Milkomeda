package com.github.yizzuide.milkomeda.universe.context;

import com.github.yizzuide.milkomeda.comet.CometAspect;
import com.github.yizzuide.milkomeda.comet.WebCometData;
import com.github.yizzuide.milkomeda.comet.XCometData;
import org.springframework.aop.framework.AopContext;

/**
 * AopContextHolder
 *
 * @author yizzuide
 * @since 1.13.4
 * @version 1.13.9
 * Create at 2019/10/24 21:17
 */
public class AopContextHolder {
    /**
     * 获得当前切面代理对象
     * <br>使用前通过<code>@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)</code>开启代理曝露
     * @param clazz 当前类
     * @param <T>   当前类型
     * @return  代理对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T self(Class<T> clazz) {
        return  (T)AopContext.currentProxy();
    }

    /**
     * 获取控制层采集数据
     * @return WebCometData
     */
    public static WebCometData getWebCometData() {
        return CometAspect.getCurrentWebCometData();
    }

    /**
     * 获取服务层采集数据
     * @return XCometData
     */
    public static XCometData getXCometData() {
        return CometAspect.getCurrentXCometData();
    }
}
