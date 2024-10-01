package com.linkwiki.auth.service;

import com.linkwiki.Member.domain.Member;
import com.linkwiki.Member.repository.MemberRepository;
import com.linkwiki.auth.domain.RefreshToken;
import com.linkwiki.auth.dto.LoginTokens;
import com.linkwiki.auth.dto.request.LoginRequest;
import com.linkwiki.auth.dto.request.SignUpRequest;
import com.linkwiki.auth.infrastructure.JwtProvider;
import com.linkwiki.auth.repository.RefreshTokenRepository;
import com.linkwiki.global.exception.AuthException;
import com.linkwiki.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginTokens login(final LoginRequest loginRequest) {
        // 1. 아이디 일치 여부
        final Member member = memberRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new AuthException(ErrorCode.FAILED_TO_LOGIN));

        // 2. 패스워드 일치 여부
        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new AuthException(ErrorCode.FAILED_TO_LOGIN);
        }

        // 3. 토큰 생성
        return generateTokensByMemberId(member.getId());
    }

    public LoginTokens signUp(final SignUpRequest signUpRequest) {
        // 1. 아이디 중복 여부
        if (memberRepository.existsByUsername(signUpRequest.getUsername())) {
            throw new AuthException(ErrorCode.DUPLICATE_USERID);
        }
        // 3. 비밀번호 암호화
        final String encodePassword = passwordEncoder.encode(signUpRequest.getPassword());
        // 3. 멤머 저장 + 닉네임 중복 체크
        final Member member = save(signUpRequest.getUsername(), encodePassword);
        // 4. 토큰 생성
        return generateTokensByMemberId(member.getId());
    }

    public String renewAccessToken(final String refreshToken) {

        // 1. 리프레쉬 토큰 검증
        try {
            jwtProvider.validateToken(refreshToken);
        } catch (AuthException e) {
            throw new AuthException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        // 2. 리프레쉬 토큰 db 존재 여부 검증
        final RefreshToken findRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AuthException(ErrorCode.INVALID_REFRESH_TOKEN));
        // 3. 엑세스 토큰 재발급
        return jwtProvider.createAccessToken(findRefreshToken.getMemberId().toString());
    }

    private LoginTokens generateTokensByMemberId(final Long memberId) {
        final LoginTokens loginTokens = jwtProvider.createLoginTokens(memberId.toString());

        refreshTokenRepository.findById(memberId)
                .ifPresentOrElse(
                        refreshToken -> refreshToken.updateToken(loginTokens.getRefreshToken()),
                        () -> refreshTokenRepository.save(new RefreshToken(memberId, loginTokens.getRefreshToken()))
                );
        return loginTokens;
    }

    private Member save(final String username, final String encodePassword) {

        int tryCount = 0;
        while (tryCount < 5) {
            String nickname = generateRandomizedNickname();
            if (!memberRepository.existsByNickname(nickname)) {
                return memberRepository.save(new Member(
                        username,
                        encodePassword,
                        nickname
                ));
            }
            tryCount++;
        }
        throw new AuthException(ErrorCode.SERVER_ERROR);
    }

    private String generateRandomizedNickname() {
        return "lw_" + generateRandomNineDigitCode();
    }

    private String generateRandomNineDigitCode() {
        final int randomNumber = (int) (Math.random() * 1000000000);
        return String.format("%09d", randomNumber);
    }
}
