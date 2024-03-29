package com.github.yizzuide.milkomeda.demo.universe;

import com.github.yizzuide.milkomeda.universe.context.WebContext;
import com.github.yizzuide.milkomeda.universe.extend.web.handler.AstrolabeHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import java.util.Objects;

/**
 * RequestAstrolabeHandler
 *
 * @author yizzuide
 * <br>
 * Create at 2021/04/19 21:06
 */
@Slf4j
@Component
public class RequestAstrolabeHandler implements AstrolabeHandler {
    @Override
    public void preHandle(ServletRequest request) {
        // ((HttpServletRequest)request).getRequestURI()
        log.info("AstrolabeHandler请求前：{}", Objects.requireNonNull(WebContext.getRequest()).getRequestURI());
    }
}
