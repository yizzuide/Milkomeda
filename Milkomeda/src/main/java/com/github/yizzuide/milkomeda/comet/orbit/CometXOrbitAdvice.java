/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.comet.orbit;

import com.github.yizzuide.milkomeda.comet.core.*;
import com.github.yizzuide.milkomeda.crust.CrustContext;
import com.github.yizzuide.milkomeda.orbit.OrbitAdvice;
import com.github.yizzuide.milkomeda.orbit.OrbitInvocation;
import com.github.yizzuide.milkomeda.universe.engine.el.ELContext;
import com.github.yizzuide.milkomeda.wormhole.WormholeEvent;
import com.github.yizzuide.milkomeda.wormhole.WormholeEventBus;
import com.github.yizzuide.milkomeda.wormhole.WormholeHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This advice listen on method which annotated {@link CometX} has invoked.
 *
 * @since 3.20.0
 * @author yizzuide
 * Create at 2024/01/28 14:16
 */
@Slf4j
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class CometXOrbitAdvice implements OrbitAdvice {

    @Autowired
    private CometProperties props;

    @Override
    public Object invoke(OrbitInvocation invocation) throws Throwable {
        CometX cometX =  AnnotationUtils.findAnnotation(invocation.getMethod(), CometX.class);
        assert cometX != null;
        XCometData cometData = new XCometData();
        cometData.setName(cometX.name());
        cometData.setCode(cometX.path());
        cometData.setTag(cometX.tag());
        cometData.setClazzName(invocation.getTargetClass().getName());
        cometData.setExecMethod(invocation.getMethod().getName());
        if (CrustContext.get() != null && CrustContext.get().hasAuthenticated()) {
            try {
                cometData.setRequestType(CrustContext.usedAPI() ? CometData.REQ_TYPE_FRONT : CometData.REQ_TYPE_BACK);
                cometData.setOperator(CrustContext.getUserInfo().getUid());
            } catch (Exception e) {
                log.warn("Can't set operator because of the login context is null");
            }
        }
        XCometData prevCometData = XCometContext.peek();
        Date startDate = new Date();
        if (prevCometData != null) {
            startDate = new Date(prevCometData.getRequestTime().getTime() + 1000L);
        }
        cometData.setRequestTime(startDate);
        XCometContext.push(cometData);

        Object result = null;
        Exception ex = null;
        try {
            result = invocation.proceed();
            cometData.setStatus(props.getStatusSuccessCode());
        } catch (Exception e) {
            ex = e;
            cometData.setStatus(props.getStatusFailCode());
            cometData.setErrorInfo(e.getMessage());
            List<StackTraceElement> stackTraceElements = Arrays.stream(e.getStackTrace()).limit(5).collect(Collectors.toList());
            cometData.setTraceStack(org.apache.commons.lang3.StringUtils.join(stackTraceElements, '\n'));
        }

        Date endDate = new Date();
        long duration = endDate.getTime() - startDate.getTime();
        cometData.setDuration(duration);
        cometData.setResponseTime(endDate);
        cometData.setResult(result);
        if (cometData.getFailure() != null) {
            cometData.setStatus(props.getStatusFailCode());
            cometData.setErrorInfo(cometData.getFailure().getMessage());
            List<StackTraceElement> stackTraceElements = Arrays.stream(cometData.getFailure().getStackTrace()).limit(5).collect(Collectors.toList());
            cometData.setTraceStack(org.apache.commons.lang3.StringUtils.join(stackTraceElements, '\n'));
        }

        boolean condition = true;
        try {
            if (StringUtils.hasLength(cometX.condition())) {
                condition = ELContext.getValue(cometData, invocation, cometX.condition(), Boolean.class);
            }
            if (condition) {
                if (StringUtils.hasLength(cometX.subjectId())) {
                    Serializable recordId = ELContext.getValue(cometData, invocation, cometX.subjectId(), Serializable.class);
                    cometData.setSubjectId(recordId);
                }
                if (StringUtils.hasLength(cometX.success())) {
                    String successInfo = ELContext.getValue(cometData, invocation, cometX.success(), String.class);
                    cometData.setSuccessInfo(successInfo);
                }
                if (StringUtils.hasLength(cometX.detail())) {
                    String detailInfo =  ELContext.getValue(cometData, invocation, cometX.detail(), String.class);
                    cometData.setDetailInfo(detailInfo);
                }

                // Send DDD Event
                WormholeEventBus eventBus = WormholeHolder.getEventBus();
                if (eventBus != null) {
                    eventBus.publish(new WormholeEvent<>(invocation.getTarget(), "CometX", cometData), XCometData.ACTION);
                }
            }
        } catch (Exception e) {
            log.info("eval expression error with msg: {}", e.getMessage(), e);
        } finally {
            XCometContext.pop();
        }
        // throw the original exception
        if (ex != null) {
            throw ex;
        }
        return result;
    }
}
