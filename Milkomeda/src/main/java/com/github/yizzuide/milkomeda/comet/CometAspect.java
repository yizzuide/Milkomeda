package com.github.yizzuide.milkomeda.comet;

import com.github.yizzuide.milkomeda.universe.config.MilkomedaProperties;
import com.github.yizzuide.milkomeda.util.*;
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

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Function;

import static com.github.yizzuide.milkomeda.util.ReflectUtil.injectParam;

/**
 * CometAspect
 * 采集切面
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 1.13.11
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
    private static ThreadLocal<CometData> threadLocal = new ThreadLocal<>();
    /**
     * 服务层本地线程存储
     */
    private static ThreadLocal<CometData> threadLocalX = new ThreadLocal<>();
    /**
     * 忽略序列化的参数
     */
    private List<Class> ignoreParams;
    /**
     * 记录器
     */
    @Getter @Setter
    private CometRecorder recorder;

    {
       recorder = new CometRecorder() {};
       ignoreParams = new ArrayList<>();
       ignoreParams.addAll(Arrays.asList(CometData.class, InputStream.class, OutputStream.class,
               ServletRequest.class, ServletResponse.class));
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
        Map<String, Object> headers = new HashMap<>();
        Enumeration<String> enumeration = request.getHeaderNames();
        while (enumeration.hasMoreElements()) {
            String key = enumeration.nextElement();
            headers.put(key, request.getHeader(key));
        }
        cometData.setRequestHeaders(JSONUtil.serialize(headers));
        cometData.setRequestIP(request.getRemoteAddr());
        cometData.setDeviceInfo(request.getHeader("user-agent"));
        return applyAround(comet, cometData, threadLocal, joinPoint, request, requestTime, comet.name(), comet.tag(), (returnData) -> {
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
        return applyAround(comet, cometData, threadLocalX, joinPoint, null, requestTime, comet.name(), comet.tag(), null);
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

    private Object applyAround(Annotation comet, CometData cometData, ThreadLocal<CometData> threadLocal, ProceedingJoinPoint joinPoint,
                               HttpServletRequest request, Date requestTime, String name, String tag,
                               Function<Object, Object> mapReturnData) throws Throwable {
        cometData.setRequestTime(requestTime);
        cometData.setName(name);
        cometData.setTag(tag);
        Signature signature = joinPoint.getSignature();
        cometData.setClazzName(signature.getDeclaringTypeName());
        cometData.setExecMethod(signature.getName());
        StringBuilder params = new StringBuilder();
        Object[] args = joinPoint.getArgs();
        if (args !=  null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (hasFilter(arg)) {
                    continue;
                }
                params.append(RecognizeUtil.isCompoundType(arg) ? JSONUtil.serialize(arg) : arg);
                if (i < args.length - 1) {
                    if (i + 1 < args.length && args[i+1] instanceof CometData) {
                        continue;
                    }
                    params.append(";");
                }
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
        recorder.onRequest(cometData, cometData.getTag(), request, args);
        threadLocal.set(cometData);

        // 执行方法体
        Object returnData = joinPoint.proceed(injectParam(joinPoint, cometData, comet, false));

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

    /**
     * 参数过滤
     * @param arg 参数
     * @return true为过滤
     */
    private boolean hasFilter(Object arg) {
        for (Class ignoreParam : ignoreParams) {
            if (ignoreParam.isInstance(arg)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取控制层采集数据
     * @return WebCometData
     */
    public static WebCometData getCurrentWebCometData() {
        return (WebCometData) threadLocal.get();
    }

    /**
     * 获取服务层采集数据
     * @return XCometData
     */
    public static XCometData getCurrentXCometData() {
        return (XCometData) threadLocalX.get();
    }

    /**
     * 配置忽略的参数类型
     *
     * @param clazzList 忽略的参数类型列表
     */
    public void addFilterClass(Class... clazzList) {
        this.ignoreParams.addAll(Arrays.asList(clazzList));
    }
}
