package com.linkwiki.auth.presentation;

import com.linkwiki.auth.dto.LoginTokens;
import com.linkwiki.auth.dto.request.LoginRequest;
import com.linkwiki.auth.dto.request.SignUpRequest;
import com.linkwiki.auth.dto.response.TokenResponse;
import com.linkwiki.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.HttpHeaders.SET_COOKIE;

@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Valid final LoginRequest loginRequest,
            final HttpServletResponse response
    ) {
        final LoginTokens token = authService.login(loginRequest);

        final ResponseCookie cookie1 = ResponseCookie.from("refresh-token", token.getRefreshToken())
                .maxAge(604800)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .build();

        response.addHeader(SET_COOKIE, cookie1.toString());
        return ResponseEntity.ok().body(new TokenResponse(token.getAccessToken()));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<TokenResponse> signUp(
            @RequestBody @Valid final SignUpRequest signUpRequest,
            final HttpServletResponse response
    ) {

        LoginTokens token = authService.signUp(signUpRequest);

        final ResponseCookie cookie1 = ResponseCookie.from("refresh-token", token.getRefreshToken())
                .maxAge(604800)
                .sameSite("None")
                .secure(true)
                .httpOnly(true)
                .path("/")
                .build();

        response.addHeader(SET_COOKIE, cookie1.toString());
        return ResponseEntity.ok().body(new TokenResponse(token.getAccessToken()));
    }

    @PostMapping("/auth/token")
    public ResponseEntity<TokenResponse> refreshAccessToken(
            @CookieValue("refresh-token") final String refreshToken
    ) {
        final String accessToken = authService.renewAccessToken(refreshToken);
        return ResponseEntity.ok().body(new TokenResponse(accessToken));
    }
}