package com.linkwiki.link.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkwiki.Member.dto.response.MemberResponse;
import com.linkwiki.auth.infrastructure.JwtProvider;
import com.linkwiki.global.config.WebConfig;
import com.linkwiki.link.dto.LinkElement;
import com.linkwiki.link.dto.request.LinkCreateRequest;
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
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(value = {LinkController.class, WebConfig.class, JwtProvider.class})
class LinkControllerTest {

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
    @DisplayName("링크 생성에 성공한다.")
    void createLink_success() throws Exception {
        // given
        LinkCreateRequest linkCreateRequest = new LinkCreateRequest("링크 설명입니다.", "https://~~~", 1L, List.of("spring", "java"));
        given(linkService.createLink(any(), any())).willReturn(1L);

        // when & then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.post("/links")
                        .header(AUTHORIZATION, "Bearer access-token")
                        .content(objectMapper.writeValueAsString(linkCreateRequest))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isCreated())
                .andDo(document("links/create",
                        preprocessRequest(prettyPrint()),          // 줄 바꿈 생기게 출력
                        preprocessResponse(prettyPrint())));
    }

    @Test
    @DisplayName("링크 조회에 성공한다.")
    void search_success() throws Exception {
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
                .page(0)
                .totalPages(2)
                .totalElement(24L)
                .isFirst(true)
                .isLast(false)
                .isEmpty(true)
                .build();
        given(linkService.getLinksByTags(any(), any())).willReturn(linksResponse);

        // when & then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/links/search")
                        .param("categoryTagId", "1") // 단일 값
                        .param("tagId", List.of("1", "2").toArray(new String[0]))
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andDo(document("links/search",
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
                                fieldWithPath("links[].updatedAt").description("링크 수정일"),
                                fieldWithPath("page").description("현재 페이지"),
                                fieldWithPath("totalPages").description("전체 페이지 개수"),
                                fieldWithPath("totalElement").description("전체 요소 개수"),
                                fieldWithPath("isFirst").description("첫번째 페이지 여부"),
                                fieldWithPath("isLast").description("마지막 페이지 여부"),
                                fieldWithPath("isEmpty").description("빈값 여부")
                        )));
    }
}