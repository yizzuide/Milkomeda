package com.github.yizzuide.milkomeda.comet;

import com.github.yizzuide.milkomeda.util.HttpServletUtil;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.NetworkUtil;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * CometAspect
 * 采集切面
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 0.2.1
 * Create at 2019/04/11 19:48
 */
@Slf4j
@Aspect
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

    @Before("comet()")
    public void doBefore(JoinPoint joinPoint) throws Exception {
        CometData cometData = recorder.prototype();
        cometData.setRequestTime(new Date());
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Comet comet = ReflectUtil.getAnnotation(joinPoint, Comet.class);
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        cometData.setApiCode(comet.apiCode());
        cometData.setDescription(comet.description());
        cometData.setRequestType(comet.requestType());
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
        recorder.onRequest(cometData, request);
        threadLocal.set(cometData);
    }

    @AfterReturning(pointcut = "comet()", returning = "object")
    public void doAfterReturn(Object object) {
        CometData cometData = threadLocal.get();
        long duration = new Date().getTime() - cometData.getRequestTime().getTime();
        cometData.setDuration(String.valueOf(duration));
        cometData.setStatus("1");
        cometData.setResponseTime(new Date());
        cometData.setResponseData(HttpServletUtil.getResponseData(object));
        log.info(JSONUtil.serialize(cometData));
        recorder.onReturn(cometData);
        threadLocal.remove();
    }

    /**
     * 异常抛出后
     */
    @AfterThrowing(pointcut = "comet()", throwing = "e")
    public void afterThrowing(RuntimeException e) {
        CometData cometData = threadLocal.get();
        long duration = new Date().getTime() - cometData.getRequestTime().getTime();
        cometData.setDuration(String.valueOf(duration));
        cometData.setStatus("2");
        cometData.setResponseData(null);
        cometData.setResponseTime(new Date());
        cometData.setErrorInfo(e.fillInStackTrace().toString());
        log.error(JSONUtil.serialize(cometData));
        recorder.onThrowing(cometData);
        threadLocal.remove();
    }
}
