package com.linkwiki.auth.infrastructure;

import com.linkwiki.auth.dto.LoginTokens;
import com.linkwiki.global.exception.AuthException;
import com.linkwiki.global.exception.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
@SpringBootTest
class JwtProviderTest {

    private static final Long SAMPLE_EXPIRATION_TIME = 60000L;
    private static final Long SAMPLE_EXPIRED_TIME = 0L;
    private static final String SAMPLE_SUBJECT = "123456789";
    private static final String SAMPLE_INVALID_SECRET_KEY = "z2VjcmV0a2V5c2VjcmV0a2V5c2VjcmV0a2V5c2VjcmV0a2V5c2VjcmV0a2V5c2VjcmV0a2V5c2VjcmV0a2V5c2VjcmV0a2V5c2VjcmV0a2V5c2VjcmV0a2V5c2VjcmV0a2V5c2VjcmV0a2Vz";
    private static final String SAMPLE_HEADER_TEST = "1234";
    @Value("${jwt.secret-key}")
    private String realSecretKey;

    @Autowired
    private JwtProvider jwtProvider;


    private String makeTestJwt(final Long accessExpirationTime, final String subject, final String secretKey) {
        final Date now = new Date();
        final Date expirationTime = new Date(now.getTime() + accessExpirationTime);

        return Jwts.builder()
                .header()
                .add("test", SAMPLE_HEADER_TEST)
                .and()
                .subject(subject) // 토큰 제목
                .issuedAt(now)  // 발급 시간
                .expiration(expirationTime)  // 만료 시간
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)), Jwts.SIG.HS256) // 서명에 필요한 비밀키, 서명 알고리즘
                .compact();
    }

    @Test
    @DisplayName("토큰이 발급에 성공하며 유효성 검증에 성공한다.")
    void createAndValidate_success() {
        // given & when
        LoginTokens loginTokens = jwtProvider.createLoginTokens(SAMPLE_SUBJECT);
        String findSubject = jwtProvider.getSubject(loginTokens.getAccessToken());

        // then
        assertThat(SAMPLE_SUBJECT).isEqualTo(findSubject);
    }

    @Test
    @DisplayName("토큰의 기한이 만료되었을 때 예외가 발생한다.")
    void validateToken_ExpiredPeriodRefreshToken() {
        // given
        String jwt = makeTestJwt(SAMPLE_EXPIRED_TIME, SAMPLE_SUBJECT, realSecretKey);

        // when & then
        assertThatThrownBy(() -> jwtProvider.validateToken(jwt))
                .isInstanceOf(AuthException.class)
                .hasMessage(ErrorCode.EXPIRED_TOKEN.getMessage());
    }

    @Test
    @DisplayName("잘못된 시크릿키를 사용할 경우 예외가 발생한다.")
    void invalidSecretkey_Fail() {
        // given
        String jwt = makeTestJwt(SAMPLE_EXPIRATION_TIME, SAMPLE_SUBJECT, SAMPLE_INVALID_SECRET_KEY);

        // when & then
        assertThatThrownBy(() -> jwtProvider.validateToken(jwt))
                .isInstanceOf(AuthException.class)
                .hasMessage(ErrorCode.INVALID_TOKEN.getMessage());
    }



}