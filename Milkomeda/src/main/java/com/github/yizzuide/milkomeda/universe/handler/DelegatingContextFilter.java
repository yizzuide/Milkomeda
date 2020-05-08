package com.github.yizzuide.milkomeda.universe.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;

import javax.annotation.PostConstruct;
import javax.servlet.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DelegatingContextFilter
 * 代理上下文过滤器
 *
 * @author yizzuide
 * @since 3.4.0
 * Create at 2020/05/06 11:12
 */
public class DelegatingContextFilter implements Filter {

    @Autowired(required = false)
    private List<AstrolabeHandler> astrolabeHandlers = new ArrayList<>();

    @PostConstruct
    public void init() {
        astrolabeHandlers = astrolabeHandlers.stream()
                .sorted(OrderComparator.INSTANCE.withSourceProvider(ha -> ha)).collect(Collectors.toList());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        for (AstrolabeHandler astrolabeHandler : astrolabeHandlers) {
            astrolabeHandler.preHandle(request);
        }
        chain.doFilter(request, response);
        for (AstrolabeHandler astrolabeHandler : astrolabeHandlers) {
            astrolabeHandler.postHandle(request, response);
        }
    }
}
