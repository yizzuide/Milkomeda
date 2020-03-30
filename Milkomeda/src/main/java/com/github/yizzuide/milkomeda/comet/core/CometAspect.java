package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.universe.config.MilkomedaProperties;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.util.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.*;
import java.util.function.Function;

/**
 * CometAspect
 * 采集切面
 *
 * @author yizzuide
 * @since 0.2.0
 * @version 3.0.0
 * Create at 2019/04/11 19:48
 */
@Slf4j
@Aspect
@Order(-99)
public class CometAspect {

    @Autowired
    private MilkomedaProperties milkomedaProperties;

    @Autowired
    private CometProperties cometProperties;

    /**
     * 请求参数解析本地线程存储
     */
    static ThreadLocal<String> resolveThreadLocal = new ThreadLocal<>();
    /**
     * 控制器层本地线程存储
     */
    // 官方推荐使用private static能减少弱引用对GC的影响
    private static ThreadLocal<CometData> threadLocal = new ThreadLocal<>();
    /**
     * 服务层本地线程存储
     */
    private static ThreadLocal<CometData> threadLocalX = new ThreadLocal<>();
    /**
     * 忽略序列化的参数
     */
    private List<Class<?>> ignoreParams;
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
    @Pointcut("@annotation(com.github.yizzuide.milkomeda.comet.core.Comet)")
    public void comet() {}

    @Pointcut("@annotation(com.github.yizzuide.milkomeda.comet.core.CometX)")
    public void cometX() {}

    @Around("comet()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Date requestTime = new Date();
        Comet comet = ReflectUtil.getAnnotation(joinPoint, Comet.class);
        // 获取记录原型对象
        HttpServletRequest request = WebContext.getRequest();
        WebCometData cometData = WebCometData.createFormRequest(request, comet.prototype(), cometProperties.isEnableReadRequestBody());
        cometData.setApiCode(comet.apiCode());
        cometData.setDescription(StringUtils.isEmpty(comet.name()) ? comet.description() : comet.name());
        cometData.setRequestType(comet.requestType());
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

    @SuppressWarnings("rawtypes")
    private Object applyAround(CometData cometData, ThreadLocal<CometData> threadLocal, ProceedingJoinPoint joinPoint,
                               HttpServletRequest request, Date requestTime, String name, String tag,
                               Function<Object, Object> mapReturnData) throws Throwable {
        cometData.setRequest(request);
        cometData.setRequestTime(requestTime);
        cometData.setName(name);
        cometData.setTag(tag);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        cometData.setClazzName(signature.getDeclaringTypeName());
        cometData.setExecMethod(signature.getName());
        Map<String, Object> params = new HashMap<>();
        // 获取参数名
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();
        if (args !=  null && args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                String argName = parameterNames[i];
                Object argValue = args[i];
                if (hasFilter(argValue)) {
                    continue;
                }
                params.put(argName, argValue);
            }
            cometData.setRequestData(JSONUtil.serialize(params));
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
        Object returnData = joinPoint.proceed();

        long duration = new Date().getTime() - cometData.getRequestTime().getTime();
        cometData.setDuration(String.valueOf(duration));
        cometData.setStatus("1");
        cometData.setResponseTime(new Date());
        if (returnData != null) {
            // returnData应用map转换类型
            if (mapReturnData != null) {
                returnData = mapReturnData.apply(returnData);
            }

            // 记录返回数据
            if (returnData instanceof ResponseEntity) {
                Object body = ((ResponseEntity) returnData).getBody();
                cometData.setResponseData(body instanceof String ? (String) body : JSONUtil.serialize(body));
            } else {
                cometData.setResponseData(returnData instanceof String ? (String) returnData : JSONUtil.serialize(returnData));
            }
        }

        // 开始回调
        Object returnObj = recorder.onReturn(cometData, returnData);
        // 修正返回值
        returnObj = returnObj == null ? returnData : returnObj;
        if (milkomedaProperties.isShowLog()) {
            log.info("Comet:- afterReturn: {}", JSONUtil.serialize(cometData));
        }
        threadLocal.remove();
        return returnObj;
    }

    public static String resolveRequestParams(HttpServletRequest request, boolean formBody) {
        String requestData = HttpServletUtil.getRequestData(request);
        // 如果form方式获取为空，取消息体内容
        if (formBody && "{}".equals(requestData)
                && request instanceof CometRequestWrapper) {
            // 从请求包装里获取
            String body = ((CometRequestWrapper) request).getBodyString();
            // 删除换行符
            body = body == null ? "" : body.replaceAll("\\n?\\t?", "");
           return body;
        }
        return requestData;
    }

    /**
     * 参数过滤
     * @param arg 参数
     * @return true为过滤
     */
    private boolean hasFilter(Object arg) {
        for (Class<?> ignoreParam : ignoreParams) {
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
    public void addFilterClass(Class<?>... clazzList) {
        this.ignoreParams.addAll(Arrays.asList(clazzList));
    }
}
