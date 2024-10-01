package com.linkwiki.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkwiki.auth.dto.LoginTokens;
import com.linkwiki.auth.dto.request.LoginRequest;
import com.linkwiki.auth.dto.request.SignUpRequest;
import com.linkwiki.auth.infrastructure.JwtProvider;
import com.linkwiki.auth.service.AuthService;
import com.linkwiki.global.config.WebConfig;
import com.linkwiki.global.exception.AuthException;
import com.linkwiki.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(value = {AuthController.class, WebConfig.class, JwtProvider.class})
class AuthControllerTest {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    @MockBean
    private AuthService authService;
    @MockBean
    private JwtProvider jwtProvider;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    @Test
    @DisplayName("로그인에 성공한다.")
    void login_success() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("username", "password");
        LoginTokens loginTokens = new LoginTokens(ACCESS_TOKEN, REFRESH_TOKEN);
        given(authService.login(any())).willReturn(loginTokens);

        // when & then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andDo(document("auth/login",
                        preprocessRequest(prettyPrint()),          // 줄 바꿈 생기게 출력
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("accessToken").description("엑세스 토큰")
                        )));
    }

    @Test
    @DisplayName("아이디가 한 글자인 경우 예외가 발생한다.")
    void login_fail_1() throws Exception {
        // given
        LoginRequest loginRequest = new LoginRequest("1", "password");
        LoginTokens loginTokens = new LoginTokens(ACCESS_TOKEN, REFRESH_TOKEN);
        given(authService.login(any())).willReturn(loginTokens);

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/login")
                        .content(objectMapper.writeValueAsString(loginRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("아이디는 5~30자까지 입력할 수 있습니다."));
    }

    @Test
    @DisplayName("회원가입에 성공한다.")
    void signUp_success() throws Exception {
        // given
        SignUpRequest signUpRequest = new SignUpRequest("username", "password");
        LoginTokens loginTokens = new LoginTokens(ACCESS_TOKEN, REFRESH_TOKEN);
        given(authService.signUp(any())).willReturn(loginTokens);

        // when & then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/sign-up")
                        .content(objectMapper.writeValueAsString(signUpRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andDo(document("auth/sign-up",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("accessToken").description("엑세스 토큰")
                        )));
    }

    @Test
    @DisplayName("토큰 갱신에 성공한다.")
    void refreshAccessToken_success() throws Exception {
        // given
        given(authService.renewAccessToken(any())).willReturn(ACCESS_TOKEN);

        // when & then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/auth/token")
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", REFRESH_TOKEN)))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andDo(document("auth/token",
                        preprocessRequest(prettyPrint()),
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("accessToken").description("엑세스 토큰")
                        )));
    }

    @Test
    @DisplayName("토큰 갱신시 유효하지 않은 리프레쉬 토큰일 경우 예외가 발생한다.")
    void refreshAccessToken_fail_INVALID_TOKEN() throws Exception {
        // given
        willThrow(new AuthException(ErrorCode.INVALID_TOKEN)).given(authService).renewAccessToken(any());

        // when & then
        mockMvc.perform(MockMvcRequestBuilders.post("/auth/token")
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", REFRESH_TOKEN)))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(ErrorCode.INVALID_TOKEN.getCode()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(ErrorCode.INVALID_TOKEN.getMessage()));
    }
}