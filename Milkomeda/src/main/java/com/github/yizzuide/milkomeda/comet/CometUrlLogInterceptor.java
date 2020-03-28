package com.github.yizzuide.milkomeda.comet;

import com.github.yizzuide.milkomeda.util.DataTypeConvertUtil;
import com.github.yizzuide.milkomeda.util.PlaceholderResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CometUrlLogInterceptor
 *
 * @author yizzuide
 * @since 2.8.0
 * Create at 2020/03/28 01:08
 */
@Slf4j
public class CometUrlLogInterceptor extends HandlerInterceptorAdapter {

    private PlaceholderResolver placeholderResolver = PlaceholderResolver.getResolver("{", "}");

    @Autowired(required = false)
    private CometUrlLogResolver cometUrlLogResolver;

    private List<CometProperties.Strategy> strategyList;

    private CometProperties props;

    public CometUrlLogInterceptor(CometProperties props) {
        this.props = props;
    }

    @PostConstruct
    public void init() {
        List<CometProperties.Strategy> strategyList = this.props.getUrlLog().getStrategy();
        if (CollectionUtils.isEmpty(strategyList)) {
            return;
        }
        // make pref
        List<String> wildSymbols = Arrays.asList("*", "/**");
        this.strategyList = strategyList.stream().peek(s -> {
            if (CollectionUtils.containsAny(s.getPaths(), wildSymbols)) {
                s.setPaths(Collections.singletonList("/**"));
            }
            List<String> placeHolders = s.getCacheKeys() == null ?
                    placeholderResolver.getPlaceHolders(s.getTpl()) : s.getCacheKeys();
            s.setCacheKeys(placeHolders);
        }).sorted((s1, s2) -> {
            // 前面包含/**向后移
            if (CollectionUtils.containsAny(s1.getPaths(), wildSymbols)) return 1;
            // 后面包含/**，保持不变
            if (CollectionUtils.containsAny(s2.getPaths(), wildSymbols)) return -1;
            return 0;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        CometProperties.UrlLog urlLog = this.props.getUrlLog();
        List<String> exclude = urlLog.getExclude();
        String requestURI = request.getRequestURI();
        if (!CollectionUtils.isEmpty(exclude)) {
            if (exclude.contains(requestURI)) {
                return true;
            }
        }
        List<CometProperties.Strategy> strategyList = this.strategyList;
        if (CollectionUtils.isEmpty(strategyList)) {
            return true;
        }
        String method = request.getMethod();
        String requestParams = CometAspect.resolveThreadLocal.get();
        requestParams = requestParams == null ?
                CometAspect.resolveRequestParams(CometHolder.getProps().isEnableReadRequestBody()) : requestParams;
        String token = request.getHeader("token");
        CometUrlLogData urlLogData = new CometUrlLogData();
        urlLogData.setUri(requestURI);
        urlLogData.setMethod(method);
        urlLogData.setParams(requestParams);
        urlLogData.setToken(token);

        for (CometProperties.Strategy strategy : strategyList) {
            if (!CollectionUtils.isEmpty(strategy.getPaths())) {
                boolean matched = false;
                for (String path : strategy.getPaths()) {
                    if (StringUtils.isEmpty(path)) continue;
                    // 去除最后一个/
                    String lastChar = path.substring(path.length() - 1);
                    if (path.length() > 1 && "/".equals(lastChar)) {
                        path = path.substring(0, path.length() - 1);
                    }

                    // 多路径匹配
                    boolean subPathWild = path.length() > 2 && "**".equals(path.substring(path.length() - 2));
                    if (subPathWild) {
                        String pathPrefix = path.substring(0, path.length() - 3);
                        if (requestURI.startsWith(pathPrefix)) {
                            matched = true;
                            break;
                        }
                    }

                    // 一个子路径匹配
                    boolean wordWild = "*".equals(lastChar);
                    if (wordWild) {
                        String pathPrefix = path.substring(0, path.length() - 1);
                        if (requestURI.startsWith(pathPrefix)) {
                            matched = true;
                            break;
                        }
                    }

                    // 完全路径匹配
                    if (requestURI.equals(path)) {
                        matched = true;
                        break;
                    }
                }
                if (!matched) continue;
                List<String> placeHolders = strategy.getCacheKeys();
                List<String> ignorePlaceHolders = Arrays.asList("uri", "method", "params");
                Map<String, Object> map = DataTypeConvertUtil.beanToMap(urlLogData);
                for (String placeHolder : placeHolders) {
                    if (ignorePlaceHolders.contains(placeHolder)) continue;
                    Object value = map.get(placeHolder);
                    map.put(placeHolder, value == null ? (cometUrlLogResolver == null ? "" :
                                    String.valueOf(cometUrlLogResolver.resolver(placeHolder, request))) : value);
                }
                log.info(placeholderResolver.resolveByObject(strategy.getTpl(), map));
                break;
            }
        }
        return super.preHandle(request, response, handler);
    }
}
