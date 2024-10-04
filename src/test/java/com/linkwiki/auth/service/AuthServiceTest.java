package com.linkwiki.auth.service;

import com.linkwiki.Member.domain.Member;
import com.linkwiki.Member.repository.MemberRepository;
import com.linkwiki.auth.dto.LoginTokens;
import com.linkwiki.auth.dto.request.LoginRequest;
import com.linkwiki.auth.dto.request.SignUpRequest;
import com.linkwiki.auth.infrastructure.JwtProvider;
import com.linkwiki.global.exception.AuthException;
import com.linkwiki.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
@Sql({"/h2-truncate.sql"})
class AuthServiceTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @MockBean
    private JwtProvider jwtProvider;

    private static final String REGISTERED_USERID = "registeredUser123";
    private static final String REGISTERED_PASSWORD = "registeredPassword123!";
    private static final String REGISTERED_NICKNAME = "registeredNickname";
    private static final String UNREGISTERED_USERID = "unregisteredUser456";
    private static final String UNREGISTERED_PASSWORD = "unregisteredPassword456!";
    private static final String UNREGISTERED_NICKNAME = "unregisteredNickname";
    private static final String ACCESS_TOKEN = "accessToken";
    private static final String REFRESH_TOKEN = "refreshToken";

    @Test
    @DisplayName("로그인에 성공한다.")
    void login_success() {
        // given
        memberRepository.save(new Member(REGISTERED_USERID, passwordEncoder.encode(REGISTERED_PASSWORD), REGISTERED_NICKNAME));
        given(jwtProvider.createLoginTokens("1")).willReturn(new LoginTokens(ACCESS_TOKEN, REFRESH_TOKEN));
        LoginRequest loginRequest = new LoginRequest(REGISTERED_USERID, REGISTERED_PASSWORD);

        // when
        LoginTokens loginTokens = authService.login(loginRequest);

        // then
        assertThat(loginTokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(loginTokens.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    @DisplayName("아이디가 일치하지 않아 로그인에 실패한다.")
    void login_fail_1() {
        // given
        memberRepository.save(new Member(REGISTERED_USERID, passwordEncoder.encode(REGISTERED_PASSWORD), REGISTERED_NICKNAME));
        LoginRequest loginRequest = new LoginRequest("fail-userId", REGISTERED_PASSWORD);

        // when then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage(ErrorCode.FAILED_TO_LOGIN.getMessage());
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않아 로그인에 실패한다.")
    void login_fail_2() {
        // given
        memberRepository.save(new Member(REGISTERED_USERID, passwordEncoder.encode(REGISTERED_PASSWORD), REGISTERED_NICKNAME));
        LoginRequest loginRequest = new LoginRequest(REGISTERED_USERID, "fail-password");

        // when then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage(ErrorCode.FAILED_TO_LOGIN.getMessage());
    }

    @Test
    @DisplayName("회원가입에 성공한다.")
    void signUp_success() {
        // given
        SignUpRequest signUpRequest = new SignUpRequest(UNREGISTERED_USERID, UNREGISTERED_PASSWORD);
        given(jwtProvider.createLoginTokens("1")).willReturn(new LoginTokens(ACCESS_TOKEN, REFRESH_TOKEN));

        // when
        LoginTokens loginTokens = authService.signUp(signUpRequest);

        // then
        assertThat(memberRepository.existsByUsername(UNREGISTERED_USERID)).isTrue();
        assertThat(loginTokens.getAccessToken()).isEqualTo(ACCESS_TOKEN);
        assertThat(loginTokens.getRefreshToken()).isEqualTo(REFRESH_TOKEN);
    }

    @Test
    @DisplayName("유저아이디가 이미 존재할 경우 회원가입에 실패한다.")
    void signUp_fail_1() {
        // given
        memberRepository.save(new Member(REGISTERED_USERID, passwordEncoder.encode(REGISTERED_PASSWORD), REGISTERED_NICKNAME));
        SignUpRequest signUpRequest = new SignUpRequest(REGISTERED_USERID, UNREGISTERED_PASSWORD);

        // when then
        assertThatThrownBy(() -> authService.signUp(signUpRequest))
                .isInstanceOf(AuthException.class)
                .hasMessage(ErrorCode.DUPLICATE_USERID.getMessage());
    }

    @Test
    @DisplayName("토큰 갱신에 성공한다.")
    void renewAccessToken_success() {
        // given
        memberRepository.save(new Member(REGISTERED_USERID, passwordEncoder.encode(REGISTERED_PASSWORD), REGISTERED_NICKNAME));
        given(jwtProvider.createLoginTokens("1")).willReturn(new LoginTokens("accessToken", "refreshToken"));
        String refreshToken = authService.login(new LoginRequest(REGISTERED_USERID, REGISTERED_PASSWORD)).getRefreshToken();

        // when
        given(jwtProvider.validateToken(refreshToken)).willReturn(null);
        given(jwtProvider.createAccessToken("1")).willReturn("accessToken");
        String accessToken = authService.renewAccessToken(refreshToken);

        // then
        assertThat(accessToken).isEqualTo("accessToken");
    }

    @Test
    @DisplayName("토큰 갱신 시, DB에 존재하지 않는 리프레시 토큰을 사용할 경우 예외가 발생한다.")
    void renewAccessToken_INVALID_REFRESH_TOKEN() {

        // given
        given(jwtProvider.validateToken("")).willReturn(null);

        // when & then
        assertThatThrownBy(() -> authService.renewAccessToken(""))
                .isInstanceOf(AuthException.class)
                .hasMessage(ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
    }
}