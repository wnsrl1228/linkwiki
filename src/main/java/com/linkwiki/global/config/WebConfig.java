package com.linkwiki.global.config;

import com.linkwiki.auth.AuthHandlerInterceptor;
import com.linkwiki.auth.LoginMemberArgumentResolver;
import com.linkwiki.auth.infrastructure.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@RequiredArgsConstructor
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtProvider jwtProvider;

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowCredentials(true) // 쿠키,세션 허용 여부
                .exposedHeaders(HttpHeaders.LOCATION); // 리다이렉션 허용 여부
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthHandlerInterceptor(jwtProvider))
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/tag/autocomplete",
                        "/links/search",
                        "/login",
                        "/auth/token",
                        "/sign-up",
                        "/*.ico",
                        "/healthy/check",
                        "/error"
                ); // 핸들러가 실행되면 안되는 애들
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver(jwtProvider));
    }
}
