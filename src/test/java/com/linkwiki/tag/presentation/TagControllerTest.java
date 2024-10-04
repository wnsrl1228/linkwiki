package com.linkwiki.tag.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linkwiki.auth.infrastructure.JwtProvider;
import com.linkwiki.global.config.WebConfig;
import com.linkwiki.tag.dto.TagElement;
import com.linkwiki.tag.dto.response.TagsResponse;
import com.linkwiki.tag.service.TagService;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureRestDocs
@WebMvcTest(value = {TagController.class, WebConfig.class, JwtProvider.class})
class TagControllerTest {

    @MockBean
    private TagService tagService;
    @MockBean
    private JwtProvider jwtProvider;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;



    @Test
    @DisplayName("태그 자동완성 조회에 성공한다.")
    void autoComplete_success() throws Exception {

        TagElement tag1 = TagElement.builder()
                .id(1L)
                .name("spring")
                .build();

        TagElement tag2 = TagElement.builder()
                .id(2L)
                .name("spring boot")
                .build();

        TagsResponse tagsResponse = TagsResponse.builder()
                .tags(List.of(tag1, tag2))
                .build();

        given(tagService.getTagsByKeyword(any())).willReturn(tagsResponse);

        // when & then
        ResultActions resultActions = mockMvc.perform(MockMvcRequestBuilders.get("/tag/autocomplete")
                        .param("keyword", "s") // 단일 값
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        resultActions
                .andExpect(status().isOk())
                .andDo(document("tag/autocomplete",
                        preprocessRequest(prettyPrint()),          // 줄 바꿈 생기게 출력
                        preprocessResponse(prettyPrint()),
                        responseFields(
                                fieldWithPath("tags").description("리스트 형태로 반환된 태그 정보들"),
                                fieldWithPath("tags[].id").description("태그의 ID"),
                                fieldWithPath("tags[].name").description("태그의 이름")
                        )));
    }
}