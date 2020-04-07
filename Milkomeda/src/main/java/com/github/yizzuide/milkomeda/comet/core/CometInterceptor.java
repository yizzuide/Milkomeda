package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.comet.collector.CometCollectorProperties;
import com.github.yizzuide.milkomeda.comet.collector.TagCollector;
import com.github.yizzuide.milkomeda.comet.logger.CometLoggerData;
import com.github.yizzuide.milkomeda.comet.logger.CometLoggerPathMatcher;
import com.github.yizzuide.milkomeda.comet.logger.CometLoggerProperties;
import com.github.yizzuide.milkomeda.comet.logger.CometLoggerResolver;
import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import com.github.yizzuide.milkomeda.universe.yml.YmlAliasNode;
import com.github.yizzuide.milkomeda.universe.yml.YmlParser;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.NetworkUtil;
import com.github.yizzuide.milkomeda.util.PlaceholderResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CometInterceptor
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/03/28 01:08
 */
@Slf4j
public class CometInterceptor extends HandlerInterceptorAdapter implements ApplicationContextAware {

    @Autowired
    private CometProperties cometProperties;

    @Autowired(required = false)
    private CometCollectorProperties cometCollectorProperties;

    @Autowired(required = false)
    private CometLoggerProperties cometLoggerProperties;

    @Autowired(required = false)
    private CometLoggerResolver cometLoggerResolver;

    // 占位符解析器
    private PlaceholderResolver placeholderResolver;

    // logger策略
    private List<CometLoggerProperties.Strategy> strategyList;

    // 线程日志记录
    private static ThreadLocal<CometData> threadLocal;

    // tag收集器
    private Map<String, TagCollector> tagCollectorMap;

    @PostConstruct
    public void init() {
        CometLoggerProperties logger = cometLoggerProperties;
        if (logger == null) {
            return;
        }
        placeholderResolver = PlaceholderResolver.getResolver(logger.getPrefix(), logger.getSuffix());
        List<CometLoggerProperties.Strategy> strategyList = logger.getStrategy();
        if (CollectionUtils.isEmpty(strategyList)) {
            return;
        }

        // make pref
        this.strategyList = strategyList.stream().peek(s -> {
            if (CollectionUtils.containsAny(s.getPaths(), CometLoggerPathMatcher.getMatchWildSymbols())) {
                s.setPaths(CometLoggerPathMatcher.getWildSymbols());
            }
            List<String> placeHolders = s.getCacheKeys() == null ?
                    placeholderResolver.getPlaceHolders(s.getTpl()) : s.getCacheKeys();
            s.setCacheKeys(placeHolders);
        }).sorted((s1, s2) -> {
            // 前面包含/**向后移
            if (CollectionUtils.containsAny(s1.getPaths(), CometLoggerPathMatcher.getMatchWildSymbols())) return 1;
            // 后面包含/**，保持不变
            if (CollectionUtils.containsAny(s2.getPaths(), CometLoggerPathMatcher.getMatchWildSymbols())) return -1;
            return 0;
        }).collect(Collectors.toList());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (this.cometCollectorProperties == null || !this.cometCollectorProperties.isEnableTag()) {
            return;
        }

        Map<String, CometCollectorProperties.Tag> tagMap = cometCollectorProperties.getTags();
        this.tagCollectorMap = tagMap.keySet().stream()
                .collect(Collectors.toMap(Object::toString, tagName -> applicationContext.getBean(tagName, TagCollector.class)));
        threadLocal = new ThreadLocal<>();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (this.cometLoggerProperties != null) {
            PulsarHolder.getPulsar().post(() -> printLog(request));
        }

        if (cometCollectorProperties != null && cometCollectorProperties.isEnableTag() && !CollectionUtils.isEmpty(this.tagCollectorMap)) {
            collectPreLog(request);
        }
        return super.preHandle(request, response, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        if (cometCollectorProperties == null || !cometCollectorProperties.isEnableTag() ||
                CollectionUtils.isEmpty(this.tagCollectorMap) || threadLocal.get() == null) {
            return;
        }
        // 获取ResponseEntity返回值
        Object body = request.getAttribute("comet.collect.body");
        if (body == null) {
            // 从ResponseWrapper获取
            CometResponseWrapper responseWrapper =
                    WebUtils.getNativeResponse(response, CometResponseWrapper.class);
            if (responseWrapper != null) {
                String content = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                String contentType = responseWrapper.getResponse().getContentType();
                // JSON -> Map
                if (contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
                    body = JSONUtil.parseMap(content, String.class, Object.class);
                } else { // String!
                    body = content;
                }
            }
        }
        this.collectPostLog(body, ex);
    }

    @SuppressWarnings("all")
    private void collectPostLog(Object body, Exception ex) {
        CometData cometData = threadLocal.get();
        Date now = new Date();
        long duration = now.getTime() - cometData.getRequestTime().getTime();
        cometData.setDuration(String.valueOf(duration));
        cometData.setResponseTime(now);
        CometCollectorProperties.Tag tag = cometCollectorProperties.getTags().get(cometData.getTag());
        TagCollector tagCollector = tagCollectorMap.get(cometData.getTag());

        // 如果响应消息体为空，按成功处理
        if (body == null) {
            cometData.setStatus(cometProperties.getStatusSuccessCode());
            cometData.setResponseData(null);
            tagCollector.onSuccess(cometData);
            threadLocal.remove();
            return;
        }

        Map<String, YmlAliasNode> aliasNodeMap = null;
        Map<String, Object> bodyMap = null;
        boolean isResponseOk = false;
        // 检测响应码是否有成功
        if (ex == null && !CollectionUtils.isEmpty(tag.getExceptionMonitor())) {
            // Response text/plain
            if (body instanceof String) {
                isResponseOk = true;
            } else { // ResponseEntity || Response application/json
                // Custom Object -> Map
                if (!(body instanceof Map)) {
                    body = DataTypeConvertUtil.beanToMap(body);
                }
                bodyMap = (Map<String, Object>) body;
                // 解析别名配置项
                aliasNodeMap = YmlParser.parseAliasMap(tag.getExceptionMonitor());
                YmlAliasNode ignoreCodeNode = aliasNodeMap.get("ignore-code");
                Object code = bodyMap.get(ignoreCodeNode.getKey());
                // 忽略的code相同，则不是异常
                isResponseOk = String.valueOf(code).equals(String.valueOf(ignoreCodeNode.getValue()));
            }
        }
        // 有异常 或 响应码不成功
        if (ex != null || !isResponseOk) {
            cometData.setStatus(cometProperties.getStatusFailCode());
            cometData.setResponseData(null);
            // 如果有异常
            if (ex != null) {
                cometData.setErrorInfo(ex.getMessage());
                StackTraceElement[] stackTrace = ex.getStackTrace();
                if (stackTrace.length > 0) {
                    String errorStack = String.format("exception happened: %s \n invoke root: %s", stackTrace[0], stackTrace[stackTrace.length - 1]);
                    cometData.setTraceStack(errorStack);
                }
            } else {
                // 从body里取异常
                Object errorStatckMsg = null;
                Object errorStack = null;
                if (bodyMap != null) {
                    YmlAliasNode errorStatckMsgNode = aliasNodeMap.get("error-stack-msg");
                    errorStatckMsg = errorStatckMsgNode == null ? null : bodyMap.get(errorStatckMsgNode.getKey());
                    YmlAliasNode errorStackNode = aliasNodeMap.get("error-stack");
                    errorStack = errorStackNode == null ? null : bodyMap.get(errorStackNode.getKey());
                }
                cometData.setErrorInfo(errorStatckMsg == null ? null : errorStatckMsg.toString());
                cometData.setTraceStack(errorStack == null ? null : errorStack.toString());
            }
            tagCollector.onFailure(cometData);
        } else {  // Response OK
            cometData.setStatus(cometProperties.getStatusSuccessCode());
            cometData.setResponseData(body instanceof String ? body.toString() : JSONUtil.serialize(body));
            tagCollector.onSuccess(cometData);
        }
        threadLocal.remove();
    }

    private void collectPreLog(HttpServletRequest request) {
        String selectTag = null;
        Map<String, CometCollectorProperties.Tag> tagMap = cometCollectorProperties.getTags();
        for (Map.Entry<String, CometCollectorProperties.Tag> tag : tagMap.entrySet()) {
            if (!CollectionUtils.isEmpty(tag.getValue().getExclude())) {
                if (CometLoggerPathMatcher.match(tag.getValue().getExclude(), request.getRequestURI())) {
                    continue;
                }
            }
            if (CometLoggerPathMatcher.match(tag.getValue().getInclude(), request.getRequestURI())) {
                selectTag = tag.getKey();
                break;
            }
        }
        if (selectTag == null) {
            return;
        }
        Date requestTime = new Date();
        WebCometData cometData = WebCometData.createFormRequest(request,
                tagMap.get(selectTag).getPrototype(), cometProperties.isEnableReadRequestBody());
        cometData.setRequest(request);
        cometData.setRequestTime(requestTime);
        try {
            String host = NetworkUtil.getHost();
            cometData.setHost(host);
        } catch (UnknownHostException ignored) {
        }
        cometData.setTag(selectTag);
        this.tagCollectorMap.get(selectTag).prepare(cometData);
        threadLocal.set(cometData);
    }

    private void printLog(HttpServletRequest request) {
        CometLoggerProperties logger = this.cometLoggerProperties;
        List<String> exclude = logger.getExclude();
        String requestURI = request.getRequestURI();
        if (!CollectionUtils.isEmpty(exclude)) {
            if (exclude.contains(requestURI)) {
                return;
            }
        }
        List<CometLoggerProperties.Strategy> strategyList = this.strategyList;
        if (CollectionUtils.isEmpty(strategyList)) {
            return;
        }
        String method = request.getMethod();
        String requestParams = CometAspect.resolveThreadLocal.get();
        requestParams = requestParams == null ?
                CometAspect.resolveRequestParams(request, CometHolder.getProps().isEnableReadRequestBody()) : requestParams;
        String token = request.getHeader("token");
        CometLoggerData urlLogData = new CometLoggerData();
        urlLogData.setUri(requestURI);
        urlLogData.setMethod(method);
        urlLogData.setParams(requestParams);
        urlLogData.setToken(token);
        for (CometLoggerProperties.Strategy strategy : strategyList) {
            if (CollectionUtils.isEmpty(strategy.getPaths()) ||
                    !CometLoggerPathMatcher.match(strategy.getPaths(), requestURI)) {
                continue;
            }
            List<String> placeHolders = strategy.getCacheKeys();
            List<String> ignorePlaceHolders = Arrays.asList("uri", "method", "params");
            Map<String, Object> map = DataTypeConvertUtil.beanToMap(urlLogData);
            for (String placeHolder : placeHolders) {
                if (ignorePlaceHolders.contains(placeHolder)) continue;
                Object value = map.get(placeHolder);
                map.put(placeHolder, value == null ? (cometLoggerResolver == null ? "" :
                        String.valueOf(cometLoggerResolver.resolver(placeHolder, request))) : value);
            }
            log.info(placeholderResolver.resolveByObject(strategy.getTpl(), map));
            break;
        }
    }
}
