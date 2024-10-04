package com.linkwiki.global.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Slf4j
@Component
public class LogAspect {

    @Pointcut("execution(* com.linkwiki..*Controller.*(..))")
    public void controller() {
    }

    @AfterReturning(pointcut = "controller()", returning = "responseEntity")
    public void afterReturning(ResponseEntity<?> responseEntity) {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
            if (request != null) {
                HttpStatusCode statusCode = responseEntity.getStatusCode();
                if (statusCode != null) {
                    log.info("[Response sent: {} {} {}]", request.getMethod(), request.getRequestURI(), statusCode);
                } else {
                    log.info("[Response sent: {} {}]", request.getMethod(), request.getRequestURI());
                }
            }
        } catch (IllegalStateException e) {}
    }
}
