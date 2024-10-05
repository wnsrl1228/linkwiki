package com.linkwiki.auth;

import com.linkwiki.auth.dto.AdminMember;
import com.linkwiki.global.exception.AuthException;
import com.linkwiki.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
public class AdminMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasAdminAnnotation = parameter.hasParameterAnnotation(Admin.class);
        boolean hasMemberType = AdminMember.class.isAssignableFrom(parameter.getParameterType());

        // Admin어노테이션이 존재하고, AdminMember 객체를 담는 경우에만 true
        return hasAdminAnnotation && hasMemberType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        log.info("[AdminMemberArgumentResolver]");
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        Long memberId = (Long) request.getAttribute("memberId");

        // 인증 과정을 거친 경우
        if (memberId != null && memberId == 1L) {
            return new AdminMember(memberId);
        }
        throw new AuthException(ErrorCode.INVALID_TOKEN);
    }
}
