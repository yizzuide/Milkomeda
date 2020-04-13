package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.metadata.BeanIds;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPathMatcher;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPlaceholderParser;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPlaceholderResolver;
import com.github.yizzuide.milkomeda.universe.parser.yml.YmlResponseOutput;
import com.github.yizzuide.milkomeda.util.JSONUtil;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ParticleFilter
 * 限制器过滤器
 *
 * @author yizzuide
 * @since 3.0.0
 * @version 3.0.2
 * Create at 2020/04/08 11:41
 */
public class ParticleFilter implements Filter {

    @Autowired
    private ParticleProperties particleProperties;

    @Autowired(required = false) @Qualifier(BeanIds.PARTICLE_RESOLVER)
    private URLPlaceholderResolver particleURLPlaceholderResolver;

    // 占位解析器
    private URLPlaceholderParser urlPlaceholderParser;

    // 跳过拦截
    private boolean skip = false;

    @PostConstruct
    public void init() {
        List<ParticleProperties.Limiter> limiters = particleProperties.getLimiters();
        if (CollectionUtils.isEmpty(limiters)) {
            skip = true;
            return;
        }
        skip = limiters.stream().allMatch(limiter -> CollectionUtils.isEmpty(limiter.getUrls()));
        if (skip) {
            return;
        }
        urlPlaceholderParser = new URLPlaceholderParser();
        urlPlaceholderParser.setCustomURLPlaceholderResolver(particleURLPlaceholderResolver);
        for (ParticleProperties.Limiter limiter : limiters) {
            if (CollectionUtils.isEmpty(limiter.getUrls()) || StringUtils.isEmpty(limiter.getKeyTpl())) {
                continue;
            }
            limiter.setCacheKeys(urlPlaceholderParser.grabPlaceHolders(limiter.getKeyTpl()));
        }
    }

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (skip || (!CollectionUtils.isEmpty(particleProperties.getExcludeUrls()) && URLPathMatcher.match(particleProperties.getExcludeUrls(), httpServletRequest.getRequestURI())) ||
                (!CollectionUtils.isEmpty(particleProperties.getIncludeUrls()) && !URLPathMatcher.match(particleProperties.getIncludeUrls(), httpServletRequest.getRequestURI()))) {
            chain.doFilter(request, response);
            return;
        }

        List<ParticleProperties.Limiter> urlLimiters = particleProperties.getLimiters().stream()
                .filter(limiter -> !CollectionUtils.isEmpty(limiter.getUrls()) && URLPathMatcher.match(limiter.getUrls(), httpServletRequest.getRequestURI()))
                .collect(Collectors.toList());
        for (ParticleProperties.Limiter limiter : urlLimiters) {
            String key = urlPlaceholderParser.parse(limiter.getKeyTpl(), httpServletRequest, null, limiter.getCacheKeys());

            Map<String, Object> returnData = limiter.getLimitHandler().limit(key, limiter.getKeyExpire().getSeconds(), particle -> {
                if (particle.isLimited()) {
                    Map<String, Object> result = new HashMap<>(9);
                    Map<String, Object> responseInfo = particleProperties.getResponse();
                    if (responseInfo == null || responseInfo.get(YmlResponseOutput.STATUS) == null) {
                        result.put(YmlResponseOutput.STATUS, 416);
                        return result;
                    }
                    int status = Integer.parseInt(responseInfo.get(YmlResponseOutput.STATUS).toString());
                    result.put(YmlResponseOutput.STATUS, status);
                    YmlResponseOutput.output(responseInfo, result, null, null, false);
                    return result;
                }
                return null;
            });

            if (returnData != null) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) response;
                httpServletResponse.setStatus(Integer.parseInt(returnData.get(YmlResponseOutput.STATUS).toString()));
                returnData.remove(YmlResponseOutput.STATUS);
                httpServletResponse.setCharacterEncoding("UTF-8");
                httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                PrintWriter writer = httpServletResponse.getWriter();
                writer.println(JSONUtil.serialize(returnData));
                writer.flush();
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
