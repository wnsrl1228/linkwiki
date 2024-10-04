package com.linkwiki.link.service;

import com.linkwiki.Member.domain.Member;
import com.linkwiki.Member.repository.MemberRepository;
import com.linkwiki.global.exception.ErrorCode;
import com.linkwiki.global.exception.InvalidException;
import com.linkwiki.link.domain.CategoryTag;
import com.linkwiki.link.domain.Link;
import com.linkwiki.link.domain.LinkHasTag;
import com.linkwiki.link.domain.LinkState;
import com.linkwiki.link.dto.LinkElement;
import com.linkwiki.link.dto.request.LinkCreateRequest;
import com.linkwiki.link.dto.request.LinkSearchRequest;
import com.linkwiki.link.dto.response.LinksResponse;
import com.linkwiki.link.repository.CategoryTagRepository;
import com.linkwiki.link.repository.LinkHasTagRepository;
import com.linkwiki.link.repository.LinkRepository;
import com.linkwiki.tag.domain.Tag;
import com.linkwiki.tag.repository.TagRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
@Sql({"/h2-truncate.sql"})
class LinkServiceTest {

    @Autowired
    private LinkService linkService;
    @Autowired
    private LinkRepository linkRepository;
    @Autowired
    private LinkHasTagRepository linkHasTagRepository;
    @Autowired
    private CategoryTagRepository categoryTagRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private TagRepository tagRepository;

    private CategoryTag categoryTag1;
    private CategoryTag categoryTag2;

    @BeforeEach
    public void setting() {
        categoryTag1 = categoryTagRepository.save(new CategoryTag("이론/개념/실습"));
        categoryTag2 = categoryTagRepository.save(new CategoryTag("버그"));
        categoryTagRepository.save(new CategoryTag("강의"));
        categoryTagRepository.save(new CategoryTag("편의/유용"));
        categoryTagRepository.save(new CategoryTag("공식문서"));
    }


    @Test
    @DisplayName("링크 등록에 성공한다.")
    void createLink_success() {
        // given
        Member member = memberRepository.save(new Member("username", "password", "nickname"));
        LinkCreateRequest linkCreateRequest = new LinkCreateRequest("설명", "url", 1L, List.of("tag1", "tag2"));
        // when
        Long linkId = linkService.createLink(member.getId(), linkCreateRequest);
        // then
        Link link = linkRepository.findById(linkId).orElse(null);
        assertThat(link.getId()).isEqualTo(1L);
        assertThat(link.getMember().getId()).isEqualTo(member.getId());
        assertThat(link.getUrl()).isEqualTo(linkCreateRequest.getUrl());
        assertThat(link.getLinkHasTags().get(0).getTag().getName()).isEqualTo("tag1");
        assertThat(link.getLinkHasTags().get(1).getTag().getName()).isEqualTo("tag2");
    }

    @Test
    @DisplayName("중복된 링크일 경우 등록에 실패한다.")
    void createLink_fail_1() {
        // given
        Member member = memberRepository.save(new Member("username", "password", "nickname"));
        LinkCreateRequest linkCreateRequest = new LinkCreateRequest("설명", "url", 1L, List.of("tag1", "tag2"));
        linkService.createLink(member.getId(), linkCreateRequest);
        // when then
        assertThatThrownBy(() -> linkService.createLink(member.getId(), linkCreateRequest))
                .isInstanceOf(InvalidException.class)
                .hasMessage(ErrorCode.DUPLICATE_LINK_URL.getMessage());
    }

    /**
     * link1 -> category1, tag1, tag2
     * link2 -> category1, tag1,
     * link3 -> category1, tag2
     * link4 -> category2, tag1
     */
    @Nested
    @DisplayName("링크 조회를 할 때")
    class findLinksByTags {

        private Tag tag1;
        private Tag tag2;
        private Link link1;
        private Link link2;
        private Link link3;
        private Link link4;

        @BeforeEach
        void setUp() {
            Member member = memberRepository.save(new Member("username", "password", "nickname"));
            tag1 = tagRepository.save(new Tag("tag1"));
            tag2 = tagRepository.save(new Tag("tag2"));

            link1 = linkRepository.save(new Link(member, categoryTag1, "url1", "description1"));
            link1.changeState(LinkState.ACTIVE);
            linkHasTagRepository.save(new LinkHasTag(link1, tag1));
            linkHasTagRepository.save(new LinkHasTag(link1, tag2));

            link2 = linkRepository.save(new Link(member, categoryTag1, "url2", "description2"));
            link2.changeState(LinkState.ACTIVE);
            linkHasTagRepository.save(new LinkHasTag(link2, tag1));

            link3 = linkRepository.save(new Link(member, categoryTag1, "url3", "description3"));
            link3.changeState(LinkState.ACTIVE);
            linkHasTagRepository.save(new LinkHasTag(link3, tag2));

            link4 = linkRepository.save(new Link(member, categoryTag2, "url4", "description4"));
            link4.changeState(LinkState.ACTIVE);
            linkHasTagRepository.save(new LinkHasTag(link4, tag1));
        }

        @Test
        @DisplayName("category_tag1 + tag1 링크 조회에 link1, link2가 출력된다.")
        void findLinksByTags_success_1() {
            // given
            LinkSearchRequest linkSearchRequest = new LinkSearchRequest(categoryTag1.getId(), List.of(tag1.getId()));
            // when
            LinksResponse linksResponse = linkService.findLinksByTags(linkSearchRequest);

            // then
            assertThat(linksResponse.getLinks().size()).isEqualTo(2);
            List<LinkElement> links = linksResponse.getLinks();
            assertThat(links.get(0).getId()).isEqualTo(link1.getId());
            assertThat(links.get(1).getId()).isEqualTo(link2.getId());
        }

        @Test
        @DisplayName("카테고리가 전체 + tag1 링크 조회에 link1, link2, link4가 출력된다.")
        void findLinksByTags_success_2() {
            // given
            LinkSearchRequest linkSearchRequest = new LinkSearchRequest(0L, List.of(tag1.getId()));
            // when
            LinksResponse linksResponse = linkService.findLinksByTags(linkSearchRequest);

            // then
            assertThat(linksResponse.getLinks().size()).isEqualTo(3);
            List<LinkElement> links = linksResponse.getLinks();
            assertThat(links.get(0).getId()).isEqualTo(link1.getId());
            assertThat(links.get(1).getId()).isEqualTo(link2.getId());
            assertThat(links.get(2).getId()).isEqualTo(link4.getId());
        }

        @Test
        @DisplayName("존재하지 않는 태그의 링크 조회에 아무것도 출력되지 않는다.")
        void findLinksByTags_success_3() {
            // given
            LinkSearchRequest linkSearchRequest = new LinkSearchRequest(0L, List.of(9999L));
            // when
            LinksResponse linksResponse = linkService.findLinksByTags(linkSearchRequest);
            // then
            assertThat(linksResponse.getLinks().size()).isEqualTo(0);
        }
    }


}