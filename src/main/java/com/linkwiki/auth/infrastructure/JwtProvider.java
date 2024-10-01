package com.linkwiki.auth.infrastructure;

import com.linkwiki.auth.dto.LoginTokens;
import com.linkwiki.global.exception.AuthException;
import com.linkwiki.global.exception.ErrorCode;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;


@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final String issuer;
    private final Long accessExpirationTime;
    private final Long refreshExpirationTime;

    public JwtProvider(
            @Value("${jwt.secret-key}") final String secretKey,
            @Value("${jwt.claims.issuer}") final String issuer,
            @Value("${jwt.access-expiration-time}") final Long accessExpirationTime,
            @Value("${jwt.refresh-expiration-time}") final Long refreshExpirationTime
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.issuer = issuer;
        this.accessExpirationTime = accessExpirationTime;
        this.refreshExpirationTime = refreshExpirationTime;
    }

    public LoginTokens createLoginTokens(final String subject) {
        String accessToken = createToken(subject, accessExpirationTime);
        String refreshToken = createToken(subject, refreshExpirationTime);

        return new LoginTokens(accessToken, refreshToken);
    }

    public String createAccessToken(final String subject) {
        return createToken(subject, accessExpirationTime);
    }

    private String createToken(final String subject, final Long expiration) {
        final Date now = new Date();
        final Date expirationTime = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .issuer(issuer)  // 발급자
                .subject(subject) // 토큰 제목
                .issuedAt(now)  // 발급 시간
                .expiration(expirationTime)  // 만료 시간
                .signWith(secretKey, Jwts.SIG.HS256) // 서명에 필요한 비밀키, 서명 알고리즘
                .compact();
    }

    public Jws<Claims> validateToken(final String jwt) {
        try {
            return parser(jwt);
        } catch (ExpiredJwtException ex) {
            throw new AuthException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException ex) {
            throw new AuthException(ErrorCode.INVALID_TOKEN);
        }
    }

    public String getSubject(final String token) {
        return parser(token)
                .getPayload()
                .getSubject();
    }

    private Jws<Claims> parser(final String jwt) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(jwt);
    }
}
