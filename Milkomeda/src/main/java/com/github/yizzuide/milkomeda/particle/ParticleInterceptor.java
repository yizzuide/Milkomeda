package com.github.yizzuide.milkomeda.particle;

import com.github.yizzuide.milkomeda.universe.metadata.BeanIds;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPathMatcher;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPlaceholderParser;
import com.github.yizzuide.milkomeda.universe.parser.url.URLPlaceholderResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * ParticleInterceptor
 *
 * @author yizzuide
 * @since 3.0.0
 * Create at 2020/04/08 11:41
 */
public class ParticleInterceptor extends HandlerInterceptorAdapter {
    @Autowired(required = false)
    private ParticleProperties particleProperties;

    @Autowired(required = false) @Qualifier(BeanIds.PARTICLE_RESOLVER)
    private URLPlaceholderResolver particleURLPlaceholderResolver;

    // 占位解析器
    private URLPlaceholderParser urlPlaceholderParser;
    // 跳过拦截
    private boolean skip = false;

    @PostConstruct
    public void init() {
        if (CollectionUtils.isEmpty(particleProperties.getLimiters())) {
            skip = true;
            return;
        }

        skip = particleProperties.getLimiters().stream().allMatch(limiter -> CollectionUtils.isEmpty(limiter.getUrls()));
        if (skip) {
            return;
        }
        urlPlaceholderParser = new URLPlaceholderParser();
        urlPlaceholderParser.setCustomURLPlaceholderResolver(particleURLPlaceholderResolver);
        for (ParticleProperties.Limiter limiter : particleProperties.getLimiters()) {
            if (CollectionUtils.isEmpty(limiter.getUrls()) || StringUtils.isEmpty(limiter.getKeyTpl())) {
                continue;
            }
            limiter.setCacheKeys(urlPlaceholderParser.grabPlaceHolders(limiter.getKeyTpl()));
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (skip) {
            return super.preHandle(request, response, handler);
        }
        for (ParticleProperties.Limiter limiter : particleProperties.getLimiters()) {
            if (!URLPathMatcher.match(limiter.getUrls(), request.getRequestURI())) {
                continue;
            }
            String key = urlPlaceholderParser.parse(limiter.getKeyTpl(), request, null, limiter.getCacheKeys()).replace("/", "-");
            // TODO 调用限制器
        }
        return super.preHandle(request, response, handler);
    }
}
