/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.universe.config.MilkomedaProperties;
import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.NetworkUtil;
import com.github.yizzuide.milkomeda.util.ReflectUtil;
import io.netty.util.concurrent.FastThreadLocal;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.context.request.async.WebAsyncTask;
import org.springframework.web.util.WebUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;

/**
 * CometAspect
 * 采集切面
 *
 * @since 0.2.0
 * @version 4.0.0
 * @author yizzuide
 * <br>
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
     * 存储请求参数解析
     */
    static final FastThreadLocal<String> resolveThreadLocal = new FastThreadLocal<>();
    /**
     * 控制器层本地线程存储
     */
    // 官方推荐使用private static能减少弱引用对GC的影响
    private static final FastThreadLocal<CometData> threadLocal = new FastThreadLocal<>();

    /**
     * 忽略序列化的参数
     */
    private final List<Class<?>> ignoreParams;

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

    @Around("comet()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Date requestTime = new Date();
        Comet comet = ReflectUtil.getAnnotation(joinPoint, Comet.class);
        // 获取记录原型对象
        HttpServletRequest request = WebContext.getRequestNonNull();
        WebCometData cometData = WebCometData.createFormRequest(request, comet.prototype(), cometProperties.isEnableReadRequestBody());
        cometData.setCode(comet.apiCode());
        cometData.setRequestType(comet.requestType());
        return applyAround(cometData, joinPoint, request, requestTime, comet.name(), comet.tag(), (returnData) -> {
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
        applyAfterThrowing(e);
    }

    private void applyAfterThrowing(Exception e) {
        CometData cometData = threadLocal.get();
        Date now = new Date();
        long duration = now.getTime() - cometData.getRequestTime().getTime();
        cometData.setStatus(cometProperties.getStatusFailCode());
        cometData.setResponseTime(now);
        cometData.setResponseData(null);
        cometData.setDuration(duration);
        cometData.setErrorInfo(e.getMessage());
        // Java 9: 使用StackWalker可以访问当前栈信息
        //StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE).walk(s->s).limit(5).toList();
        List<StackTraceElement> stackTraceElements = Arrays.stream(e.getStackTrace()).limit(5).toList();
        cometData.setTraceStack(StringUtils.join(stackTraceElements, '\n'));
        recorder.onThrowing(cometData, e);
        threadLocal.remove();
    }

    @SuppressWarnings("rawtypes")
    private Object applyAround(WebCometData cometData, ProceedingJoinPoint joinPoint,
                               HttpServletRequest request, Date requestTime, String name, String tag,
                               Function<Object, Object> mapReturnData) throws Throwable {
        cometData.setRequestTime(requestTime);
        cometData.setName(name);
        cometData.setTag(tag);
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        cometData.setClazzName(signature.getDeclaringTypeName());
        cometData.setExecMethod(signature.getName());
        Map<String, Object> methodParams = ReflectUtil.getMethodParams(joinPoint, ignoreParams);
        if (methodParams != null) {
            cometData.setRequestData(methodParams);
        }
        String host = NetworkUtil.getHost();
        cometData.setHost(host);
        if (milkomedaProperties.isShowLog()) {
            log.info("method[{}] invoke before: {}", signature.getName(), JSONUtil.serialize(cometData));
        }
        // 外部可以扩展记录自定义数据
        recorder.onRequest(cometData, cometData.getTag(), request, joinPoint.getArgs());
        threadLocal.set(cometData);

        // 执行方法体
        Object returnData = joinPoint.proceed();

        Date responseDate = new Date();
        long duration = responseDate.getTime() - cometData.getRequestTime().getTime();
        cometData.setDuration(duration);
        cometData.setStatus(cometProperties.getStatusSuccessCode());
        cometData.setResponseTime(responseDate);
        if (returnData != null) {
            // returnData应用map转换类型
            if (mapReturnData != null) {
                returnData = mapReturnData.apply(returnData);
            }

            // 记录返回数据
            if (returnData instanceof ResponseEntity) {
                Object body = ((ResponseEntity) returnData).getBody();
                cometData.setResponseData(body);
            } else {
                cometData.setResponseData(returnData);
            }
        } else {
            // 通过HttpServletResponse写出的，需要读取包装的Response消息体
            if (CometHolder.getCollectorProps() != null && CometHolder.getCollectorProps().isEnable()) {
                HttpServletResponse response = WebContext.getResponse();
                if (response != null) {
                    CometResponseWrapper responseWrapper =
                            WebUtils.getNativeResponse(response, CometResponseWrapper.class);
                    if (responseWrapper != null) {
                        cometData.setStatus(response.getStatus() == HttpStatus.OK.value() ? cometProperties.getStatusSuccessCode() : cometProperties.getStatusFailCode());
                        String content = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                        cometData.setResponseData(content);
                    }
                }
            }
        }

        // 开始回调
        Object returnObj = recorder.onReturn(cometData, returnData);
        // 是否有修改返回值
        returnObj = returnObj == null ? returnData : returnObj;
        if (milkomedaProperties.isShowLog()) {
            log.info("method[{}] invoke afterReturn: {}", signature.getName(), JSONUtil.serialize(cometData));
        }
        threadLocal.remove();
        return returnObj;
    }

    /**
     * 获取控制层采集数据
     * @return WebCometData
     */
    public static WebCometData getCurrentWebCometData() {
        return (WebCometData) threadLocal.get();
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
