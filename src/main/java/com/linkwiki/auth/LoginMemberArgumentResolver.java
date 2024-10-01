package com.linkwiki.auth;

import com.linkwiki.auth.dto.LoginMember;
import com.linkwiki.auth.infrastructure.JwtProvider;
import com.linkwiki.global.exception.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@RequiredArgsConstructor
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private final JwtProvider jwtProvider;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
        boolean hasMemberType = LoginMember.class.isAssignableFrom(parameter.getParameterType());

        // Login어노테이션이 존재하고, LoginMember 객체를 담는 경우에만 true
        return hasLoginAnnotation && hasMemberType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        final HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        Long memberId = (Long) request.getAttribute("memberId");

        // 인증 과정을 거친 경우
        if (memberId != null) {
            return new LoginMember(memberId);
        }

        // 인증 과정을 거치지 않은 경우 ex) 상세 조회시 유저의 좋아요 여부를 알고 싶은 경우, 따라서 예외를 던지면 안됨
        String token = extractTokenFromRequest(request);

        if (token == null) {
            return new LoginMember(null);
        }

        try {
            jwtProvider.validateToken(token);
        } catch (AuthException e) {
            return new LoginMember(null);
        }

        final String subject = jwtProvider.getSubject(token);
        memberId = Long.valueOf(subject);

        return new LoginMember(memberId);
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        // Authorization 헤더 값을 가져옵니다.
        String authorizationHeader = request.getHeader("Authorization");

        // 헤더가 존재하고, Bearer로 시작하는지 확인합니다.
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            // Bearer 토큰을 추출하여 반환합니다.
            return authorizationHeader.substring(7); // "Bearer " 다음의 값부터 반환합니다.
        }

        return null;
    }
}