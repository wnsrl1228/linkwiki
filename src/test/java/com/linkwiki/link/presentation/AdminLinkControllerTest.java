package com.linkwiki.link.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkwiki.Member.dto.response.MemberResponse;
import com.linkwiki.auth.infrastructure.JwtProvider;
import com.linkwiki.global.config.WebConfig;
import com.linkwiki.link.dto.LinkElement;
import com.linkwiki.link.dto.request.LinkModifyRequest;
import com.linkwiki.link.dto.request.LinkReviewRequest;
import com.linkwiki.link.dto.response.LinksResponse;
import com.linkwiki.link.service.LinkService;
import com.linkwiki.tag.dto.TagElement;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(value = {AdminLinkController.class, WebConfig.class, JwtProvider.class})
class AdminLinkControllerTest {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";

    @MockBean
    private LinkService linkService;
    @MockBean
    private JwtProvider jwtProvider;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    @BeforeEach
    void setting() {
        given(jwtProvider.validateToken(any())).willReturn(null);
        given(jwtProvider.getSubject(any())).willReturn("1");
    }

    @Test
    @DisplayName("검토중인 링크 조회에 성공한다.")
    void getLinksInReviewState_success_1() throws Exception {
        // given
        MemberResponse memberResponse = MemberResponse.builder()
                .id(1L)
                .nickname("nickname")
                .profileImgUrl("https://~~")
                .build();
        TagElement tag1 = TagElement.builder()
                .id(1L)
                .name("spring")
                .build();
        TagElement tag2 = TagElement.builder()
                .id(2L)
                .name("spring boot")
                .build();
        LinkElement linkElement = LinkElement.builder()
                .id(1L)
                .member(memberResponse)
                .categoryTag("공식문서")
                .url("https://hello.com")
                .description("링크에 대한 설명입니다.")
                .likeCount(0)
                .bookmarkCount(0)
                .clickCount(0)
                .rating(0.0f)
                .tags(List.of(tag1, tag2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        LinksResponse linksResponse = LinksResponse.builder()
                .links(List.of(linkElement))
                .build();
        given(linkService.getLinksInReviewState()).willReturn(linksResponse);

        // when & then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/admin/links")
                        .header(AUTHORIZATION, "Bearer access-token")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andDo(document("/admin/links/list",
                        preprocessRequest(prettyPrint()),          // 줄 바꿈 생기게 출력
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("links").description("리스트 형태로 반환된 링크 정보들"),
                                fieldWithPath("links[].id").description("링크의 ID"),
                                fieldWithPath("links[].member.id").description("링크를 생성한 멤버의 ID"),
                                fieldWithPath("links[].member.nickname").description("링크를 생성한 멤버의 닉네임"),
                                fieldWithPath("links[].member.profileImgUrl").description("링크를 생성한 멤버의 프로필 이미지 URL"),
                                fieldWithPath("links[].categoryTag").description("링크의 카테고리 태그"),
                                fieldWithPath("links[].url").description("링크 URL"),
                                fieldWithPath("links[].description").description("링크 설명"),
                                fieldWithPath("links[].likeCount").description("링크의 좋아요 개수"),
                                fieldWithPath("links[].bookmarkCount").description("링크의 북마크 개수"),
                                fieldWithPath("links[].clickCount").description("링크 클릭 수"),
                                fieldWithPath("links[].rating").description("링크의 평점"),
                                fieldWithPath("links[].tags[].id").description("태그의 ID"),
                                fieldWithPath("links[].tags[].name").description("태그의 이름"),
                                fieldWithPath("links[].createdAt").description("링크 생성일"),
                                fieldWithPath("links[].updatedAt").description("링크 수정일")
                        )));
    }

    @Test
    @DisplayName("검토 중인 링크 등록에 성공한다.")
    void reviewLink() throws Exception {
        // given
        LinkReviewRequest linkReviewRequest = new LinkReviewRequest(true, "링크가 등록되었습니다.");
        willDoNothing().given(linkService).reviewLink(any(), any());

        // when & then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/admin/links/1")
                        .header(AUTHORIZATION, "Bearer access-token")
                        .content(objectMapper.writeValueAsString(linkReviewRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andDo(document("/admin/links/create",
                        preprocessRequest(prettyPrint()),          // 줄 바꿈 생기게 출력
                        preprocessResponse(prettyPrint())));
    }

    @Test
    @DisplayName("검토 중인 링크 수정 페이지 데이터 조회에 성공한다.")
    void getLinkForModification() throws Exception {
        // given
        MemberResponse memberResponse = MemberResponse.builder()
                .id(1L)
                .nickname("nickname")
                .profileImgUrl("https://~~")
                .build();
        TagElement tag1 = TagElement.builder()
                .id(1L)
                .name("spring")
                .build();
        TagElement tag2 = TagElement.builder()
                .id(2L)
                .name("spring boot")
                .build();
        LinkElement linkElement = LinkElement.builder()
                .id(1L)
                .member(memberResponse)
                .categoryTag("공식문서")
                .url("https://hello.com")
                .description("링크에 대한 설명입니다.")
                .likeCount(0)
                .bookmarkCount(0)
                .clickCount(0)
                .rating(0.0f)
                .tags(List.of(tag1, tag2))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        given(linkService.getLinkById(any())).willReturn(linkElement);
        // when & then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/admin/links/1")
                        .header(AUTHORIZATION, "Bearer access-token")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andDo(document("/admin/links/findmodify",
                        preprocessRequest(prettyPrint()),          // 줄 바꿈 생기게 출력
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("id").description("링크의 ID"),
                                fieldWithPath("member.id").description("링크를 생성한 멤버의 ID"),
                                fieldWithPath("member.nickname").description("링크를 생성한 멤버의 닉네임"),
                                fieldWithPath("member.profileImgUrl").description("링크를 생성한 멤버의 프로필 이미지 URL"),
                                fieldWithPath("categoryTag").description("링크의 카테고리 태그"),
                                fieldWithPath("url").description("링크 URL"),
                                fieldWithPath("description").description("링크 설명"),
                                fieldWithPath("likeCount").description("링크의 좋아요 개수"),
                                fieldWithPath("bookmarkCount").description("링크의 북마크 개수"),
                                fieldWithPath("clickCount").description("링크 클릭 수"),
                                fieldWithPath("rating").description("링크의 평점"),
                                fieldWithPath("tags[].id").description("태그의 ID"),
                                fieldWithPath("tags[].name").description("태그의 이름"),
                                fieldWithPath("createdAt").description("링크 생성일"),
                                fieldWithPath("updatedAt").description("링크 수정일")
                        )));
    }

    @Test
    @DisplayName("검토 중인 링크 수정에 성공한다.")
    void modifyLink() throws Exception {
        // given
        LinkModifyRequest linkModifyRequest = new LinkModifyRequest(List.of("tag1", "tag2"));
        willDoNothing().given(linkService).modifyLink(any(), any());

        // when & then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.patch("/admin/links/1")
                        .header(AUTHORIZATION, "Bearer access-token")
                        .content(objectMapper.writeValueAsString(linkModifyRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andDo(document("/admin/links/modify",
                        preprocessRequest(prettyPrint()),          // 줄 바꿈 생기게 출력
                        preprocessResponse(prettyPrint())));
    }
}