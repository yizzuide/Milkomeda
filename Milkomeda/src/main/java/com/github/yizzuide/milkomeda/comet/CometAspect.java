package com.github.yizzuide.milkomeda.comet;

import com.github.yizzuide.milkomeda.util.HttpServletUtil;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.NetworkUtil;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * CometAspect
 * 采集切面
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 1.5.3
 * Create at 2019/04/11 19:48
 */
@Slf4j
@Aspect
@Order(9)
public class CometAspect {
    private ThreadLocal<CometData> threadLocal = new ThreadLocal<>();
    /**
     * 记录器
     */
    @Getter @Setter
    private CometRecorder recorder;
    {
       recorder = new CometRecorder() {};
    }

    // 切入点
    @Pointcut("@annotation(com.github.yizzuide.milkomeda.comet.Comet)")
    public void comet() {}

    @Around("comet()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Date now = new Date();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Comet comet = ReflectUtil.getAnnotation(joinPoint, Comet.class);
        // 获取记录原型对象
        CometData cometData = comet.prototype().newInstance();
        cometData.setRequestTime(now);
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        cometData.setApiCode(comet.apiCode());
        cometData.setDescription(comet.description());
        cometData.setRequestType(comet.requestType());
        cometData.setTag(comet.tag());
        cometData.setRequestData(HttpServletUtil.getRequestData(request));
        cometData.setRequestURL(request.getRequestURL().toString());
        cometData.setRequestPath(request.getServletPath());
        cometData.setRequestMethod(request.getMethod());
        Signature signature = joinPoint.getSignature();
        cometData.setExecMethod(signature.getDeclaringTypeName() + "#" +
                signature.getName());
        cometData.setHost(NetworkUtil.getHost());
        cometData.setRequestIP(request.getRemoteAddr());
        cometData.setDeviceInfo(request.getHeader("user-agent"));
        log.info("Comet:- before: {}", JSONUtil.serialize(cometData));
        recorder.onRequest(cometData, cometData.getTag(), request);
        threadLocal.set(cometData);

        // 执行方法体
        Object returnData = joinPoint.proceed();

        long duration = new Date().getTime() - cometData.getRequestTime().getTime();
        cometData.setDuration(String.valueOf(duration));
        cometData.setStatus("1");
        cometData.setResponseTime(new Date());
        // 有的请求方法为空，如第三方回调
        if (returnData != null) {
            if (returnData.getClass() == DeferredResult.class) {
                cometData.setResponseData("[DeferredResult]");
            } else if (returnData.getClass() == WebAsyncTask.class) {
                cometData.setResponseData("[WebAsyncTask]");
            } else {
                cometData.setResponseData(HttpServletUtil.getResponseData(returnData));
            }
        }
        log.info("Comet:- afterReturn: {}", JSONUtil.serialize(cometData));
        return recorder.onReturn(cometData, returnData);
    }

    /**
     * 异常抛出后
     */
    @AfterThrowing(pointcut = "comet()", throwing = "e")
    public void afterThrowing(Exception e) {
        CometData cometData = threadLocal.get();
        long duration = new Date().getTime() - cometData.getRequestTime().getTime();
        cometData.setDuration(String.valueOf(duration));
        cometData.setStatus("2");
        cometData.setResponseData(null);
        cometData.setResponseTime(new Date());
        log.error("Comet:- afterThrowing: {}", e.getMessage(), e);
        recorder.onThrowing(cometData, e);
        threadLocal.remove();
    }
}
