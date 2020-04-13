package com.github.yizzuide.milkomeda.comet.core;

import com.github.yizzuide.milkomeda.comet.collector.CometCollectorProperties;
import com.github.yizzuide.milkomeda.comet.collector.CometCollectorResponseBodyAdvice;
import com.github.yizzuide.milkomeda.comet.collector.TagCollector;
import com.github.yizzuide.milkomeda.universe.metadata.BeanIds;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPathMatcher;
import com.github.yizzuide.milkomeda.comet.logger.CometLoggerProperties;
import com.github.yizzuide.milkomeda.pulsar.PulsarHolder;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPlaceholderParser;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPlaceholderResolver;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlAliasNode;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlParser;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlResponseOutput;
import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import com.github.yizzuide.milkomeda.util.NetworkUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CometInterceptor
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.0.3
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

    @Autowired(required = false) @Qualifier(BeanIds.COMET_LOGGER_RESOLVER)
    private URLPlaceholderResolver cometLoggerURLPlaceholderResolver;

    // 占位解析器
    private URLPlaceholderParser urlPlaceholderParser;

    // logger策略
    private List<CometLoggerProperties.Strategy> strategyList;

    // 线程日志记录
    private static ThreadLocal<CometData> threadLocal;

    // tag收集器
    private Map<String, TagCollector> tagCollectorMap;

    // 异常body识别节点
    private Map<String, Map<String, YmlAliasNode>> aliasNodesMap;

    @PostConstruct
    public void init() {
        CometLoggerProperties logger = cometLoggerProperties;
        if (logger == null) {
            return;
        }
        urlPlaceholderParser = new URLPlaceholderParser(logger.getPrefix(), logger.getSuffix());
        urlPlaceholderParser.setCustomURLPlaceholderResolver(cometLoggerURLPlaceholderResolver);
        List<CometLoggerProperties.Strategy> strategyList = logger.getStrategy();
        if (CollectionUtils.isEmpty(strategyList)) {
            return;
        }

        // make pref
        this.strategyList = strategyList.stream().peek(s -> {
            if (CollectionUtils.containsAny(s.getPaths(), URLPathMatcher.getMatchWildSymbols())) {
                s.setPaths(URLPathMatcher.getWildSymbols());
            }
            Map<String, List<String>> placeHolders = s.getCacheKeys() == null ?
                    urlPlaceholderParser.grabPlaceHolders(s.getTpl()) : s.getCacheKeys();
            s.setCacheKeys(placeHolders);
        }).sorted((s1, s2) -> {
            // 前面包含/**向后移
            if (CollectionUtils.containsAny(s1.getPaths(), URLPathMatcher.getMatchWildSymbols())) return 1;
            // 后面包含/**，保持不变
            if (CollectionUtils.containsAny(s2.getPaths(), URLPathMatcher.getMatchWildSymbols())) return -1;
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

        aliasNodesMap = new HashMap<>();
        for (Map.Entry<String, CometCollectorProperties.Tag> tagCollectorEntry : cometCollectorProperties.getTags().entrySet()) {
            Map<String, Object> exceptionMonitor = tagCollectorEntry.getValue().getExceptionMonitor();
            if(CollectionUtils.isEmpty(exceptionMonitor)) {
                continue;
            }
            String tag = tagCollectorEntry.getKey();
            aliasNodesMap.put(tag, YmlParser.parseAliasMap(exceptionMonitor));
        }
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
        Object body = request.getAttribute(CometCollectorResponseBodyAdvice.REQUEST_ATTRIBUTE_BODY);
        if (body == null) {
            // 从ResponseWrapper获取
            CometResponseWrapper responseWrapper =
                    WebUtils.getNativeResponse(response, CometResponseWrapper.class);
            if (responseWrapper != null) {
                String content = new String(responseWrapper.getContentAsByteArray(), StandardCharsets.UTF_8);
                if (StringUtils.isEmpty(content)) {
                    body = null;
                } else {
                    String contentType = responseWrapper.getResponse().getContentType();
                    // JSON -> Map
                    if (contentType.startsWith(MediaType.APPLICATION_JSON_VALUE)) {
                        body = JSONUtil.parseMap(content, String.class, Object.class);
                    } else { // String!
                        body = content;
                    }
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
        String tag = cometData.getTag();
        TagCollector tagCollector = tagCollectorMap.get(tag);

        // 如果有异常（说明没有统一异常拦截响应处理）
        if (ex != null) {
            cometData.setStatus(cometProperties.getStatusFailCode());
            cometData.setResponseData(null);
            cometData.setErrorInfo(ex.getMessage());
            StackTraceElement[] stackTrace = ex.getStackTrace();
            if (stackTrace.length > 0) {
                String errorStack = String.format("exception happened: %s \n invoke root: %s", stackTrace[0], stackTrace[stackTrace.length - 1]);
                cometData.setTraceStack(errorStack);
            }
            tagCollector.onFailure(cometData);
            threadLocal.remove();
            return;
        }

        // 如果响应消息体为空，按成功处理
        if (body == null) {
            cometData.setStatus(cometProperties.getStatusSuccessCode());
            cometData.setResponseData(null);
            tagCollector.onSuccess(cometData);
            threadLocal.remove();
            return;
        }

        // 检测Body返回code
        Map<String, Object> bodyMap = null;
        boolean isResponseOk = false;
        // 检测响应码是否有成功
        Map<String, YmlAliasNode> aliasNodes = aliasNodesMap.get(tag);
        if (!CollectionUtils.isEmpty(aliasNodes)) {
            // Response text/plain
            if (body instanceof String) {
                isResponseOk = true;
            } else { // Map or Custom Return Object
                // Custom Return Object -> Map
                if (!(body instanceof Map)) {
                    body = DataTypeConvertUtil.beanToMap(body);
                }
                bodyMap = (Map<String, Object>) body;
                // 解析别名配置项
                YmlAliasNode ignoreCodeNode = aliasNodes.get("ignore-code");
                Object code = bodyMap.get(ignoreCodeNode.getKey());
                // 忽略的code相同，则不是异常
                isResponseOk = String.valueOf(code).equals(String.valueOf(ignoreCodeNode.getValue()));
            }
        }

        // 响应码不成功
        if (!isResponseOk) {
            cometData.setStatus(cometProperties.getStatusFailCode());
            cometData.setResponseData(null);
            // 从body里取异常
            Object errorStatckMsg = null;
            Object errorStack = null;
            if (bodyMap != null) {
                YmlAliasNode errorStatckMsgNode = aliasNodes.get(YmlResponseOutput.ERROR_STACK_MSG);
                cometData.setErrorInfo(errorStatckMsgNode == null ? null : DataTypeConvertUtil.extractValue(errorStatckMsgNode.getKey(), bodyMap));
                YmlAliasNode errorStackNode = aliasNodes.get(YmlResponseOutput.ERROR_STACK);
                cometData.setTraceStack(errorStackNode == null ? null : DataTypeConvertUtil.extractValue(errorStackNode.getKey(), bodyMap));
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
                if (URLPathMatcher.match(tag.getValue().getExclude(), request.getRequestURI())) {
                    continue;
                }
            }
            if (URLPathMatcher.match(tag.getValue().getInclude(), request.getRequestURI())) {
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
        String requestParams = CometAspect.resolveThreadLocal.get();
        for (CometLoggerProperties.Strategy strategy : strategyList) {
            if (CollectionUtils.isEmpty(strategy.getPaths()) ||
                    !URLPathMatcher.match(strategy.getPaths(), requestURI)) {
                continue;
            }
            log.info(urlPlaceholderParser.parse(strategy.getTpl(), request, requestParams, strategy.getCacheKeys()));
            break;
        }
    }
}
