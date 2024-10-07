package com.linkwiki.global.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MDCLoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        MDC.put("request_id", UUID.randomUUID().toString());

        // 요청 URL 구성
        String requestURI = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = requestURI + (queryString != null ? "?" + queryString : "");

        log.info("[Request received: {} {}]", request.getMethod(), fullUrl);

        filterChain.doFilter(servletRequest, servletResponse);
        MDC.clear();
    }

}
