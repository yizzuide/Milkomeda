package com.github.yizzuide.milkomeda.comet;

import com.github.yizzuide.milkomeda.universe.config.MilkomedaProperties;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.http.HttpServletRequest;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.function.Function;

/**
 * CometAspect
 * 采集切面
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 1.13.2
 * Create at 2019/04/11 19:48
 */
@Slf4j
@Aspect
@Order(-99)
public class CometAspect {

    @Autowired
    private MilkomedaProperties milkomedaProperties;

    /**
     * 控制器层本地线程存储
     */
    private ThreadLocal<CometData> threadLocal = new ThreadLocal<>();
    /**
     * 服务层本地线程存储
     */
    private ThreadLocal<CometData> threadLocalX = new ThreadLocal<>();

    /**
     * 记录器
     */
    @Getter @Setter
    private CometRecorder recorder;
    {
       recorder = new CometRecorder() {};
    }

    // 定义切入点
    @Pointcut("@annotation(com.github.yizzuide.milkomeda.comet.Comet)")
    public void comet() {}

    @Pointcut("@annotation(com.github.yizzuide.milkomeda.comet.CometX)")
    public void cometX() {}

    @Around("comet()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Date requestTime = new Date();
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        Comet comet = ReflectUtil.getAnnotation(joinPoint, Comet.class);
        // 获取记录原型对象
        WebCometData cometData = comet.prototype().newInstance();
        assert attributes != null;
        HttpServletRequest request = attributes.getRequest();
        cometData.setApiCode(comet.apiCode());
        cometData.setDescription(StringUtils.isEmpty(comet.name()) ? comet.description() : comet.name());
        cometData.setRequestType(comet.requestType());
        cometData.setRequestURL(request.getRequestURL().toString());
        cometData.setRequestPath(request.getServletPath());
        cometData.setRequestMethod(request.getMethod());
        cometData.setRequestParams(HttpServletUtil.getRequestData(request));
        cometData.setRequestIP(request.getRemoteAddr());
        cometData.setDeviceInfo(request.getHeader("user-agent"));
        return applyAround(cometData, threadLocal, joinPoint, request, requestTime, comet.name(), comet.tag(), (returnData) -> {
            if (returnData.getClass() == DeferredResult.class) {
                return "[DeferredResult]";
            }
            if (returnData.getClass() == WebAsyncTask.class) {
                return "[WebAsyncTask]";
            }
            return returnData;
        });
    }

    /**
     * 异常抛出后
     * @param e 异常
     */
    @AfterThrowing(pointcut = "comet()", throwing = "e")
    public void afterThrowing(Exception e) {
        applyAfterThrowing(e, threadLocal);
    }

    @Around("cometX()")
    public Object aroundX(ProceedingJoinPoint joinPoint)  throws Throwable {
        Date requestTime = new Date();
        CometX comet = ReflectUtil.getAnnotation(joinPoint, CometX.class);
        // 获取记录原型对象
        XCometData cometData = comet.prototype().newInstance();
        return applyAround(cometData, threadLocalX, joinPoint, null, requestTime, comet.name(), comet.tag(), null);
    }

    @AfterThrowing(pointcut = "cometX()", throwing = "e")
    public void afterThrowingX(Exception e) {
        applyAfterThrowing(e, threadLocalX);
    }

    private void applyAfterThrowing(Exception e, ThreadLocal<CometData> threadLocal) {
        CometData cometData = threadLocal.get();
        Date now = new Date();
        long duration = now.getTime() - cometData.getRequestTime().getTime();
        cometData.setStatus("2");
        cometData.setResponseTime(now);
        cometData.setResponseData(null);
        cometData.setDuration(String.valueOf(duration));
        cometData.setErrorInfo(e.getMessage());
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace.length > 0) {
            String errorStack = String.format("exception happened: %s \n invoke root: %s", stackTrace[0], stackTrace[stackTrace.length - 1]);
            cometData.setTraceStack(errorStack);
        }
        recorder.onThrowing(cometData, e);
        threadLocal.remove();
    }

    private Object applyAround(CometData cometData, ThreadLocal<CometData> threadLocal, ProceedingJoinPoint joinPoint,
                               HttpServletRequest request, Date requestTime, String name, String tag,
                               Function<Object, Object> mapReturnData) throws Throwable {
        cometData.setRequestTime(requestTime);
        cometData.setName(name);
        cometData.setTag(tag);
        Signature signature = joinPoint.getSignature();
        cometData.setClazzName(signature.getDeclaringTypeName());
        cometData.setExecMethod(signature.getName());
        StringBuilder params = new StringBuilder();
        if (joinPoint.getArgs() !=  null && joinPoint.getArgs().length > 0) {
            for (int i = 0; i < joinPoint.getArgs().length; i++) {
                params.append(JSONUtil.serialize(joinPoint.getArgs()[i])).append(";");
            }
            cometData.setRequestData(params.toString());
        }
        try {
            String host = NetworkUtil.getHost();
            cometData.setHost(host);
        } catch (UnknownHostException ignored) {
        }
        if (milkomedaProperties.isShowLog()) {
            log.info("Comet:- before: {}", JSONUtil.serialize(cometData));
        }
        // 外部可以扩展记录自定义数据
        recorder.onRequest(cometData, cometData.getTag(), request);
        threadLocal.set(cometData);

        // 执行方法体
        Object returnData = joinPoint.proceed();

        long duration = new Date().getTime() - cometData.getRequestTime().getTime();
        cometData.setDuration(String.valueOf(duration));
        cometData.setStatus("1");
        cometData.setResponseTime(new Date());
        // returnData应用map转换类型
        if (returnData != null) {
            if (mapReturnData != null) {
                returnData = mapReturnData.apply(returnData);
            }
        }
        // 开始回调
        Object returnObj = recorder.onReturn(cometData, returnData);
        // 修正返回值
        returnObj = returnObj == null ? returnData : returnObj;
        // 记录返回数据
        if (returnObj != null) {
            if (returnObj instanceof ResponseEntity) {
                Object body = ((ResponseEntity) returnObj).getBody();
                cometData.setResponseData(body instanceof String ? (String) body : JSONUtil.serialize(body));
            } else {
                cometData.setResponseData(returnObj instanceof String ? (String) returnObj : JSONUtil.serialize(returnObj));
            }
        }
        if (milkomedaProperties.isShowLog()) {
            log.info("Comet:- afterReturn: {}", JSONUtil.serialize(cometData));
        }
        threadLocal.remove();
        return returnObj;
    }
}
