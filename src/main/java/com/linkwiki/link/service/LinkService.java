package com.linkwiki.link.service;

import com.linkwiki.Member.domain.Member;
import com.linkwiki.Member.repository.MemberRepository;
import com.linkwiki.global.exception.ErrorCode;
import com.linkwiki.global.exception.InvalidException;
import com.linkwiki.link.domain.CategoryTag;
import com.linkwiki.link.domain.Link;
import com.linkwiki.link.domain.LinkHasTag;
import com.linkwiki.link.domain.LinkState;
import com.linkwiki.link.dto.request.LinkCreateRequest;
import com.linkwiki.link.dto.request.LinkSearchRequest;
import com.linkwiki.link.dto.response.LinksResponse;
import com.linkwiki.link.repository.CategoryTagRepository;
import com.linkwiki.link.repository.LinkHasTagRepository;
import com.linkwiki.link.repository.LinkRepository;
import com.linkwiki.tag.domain.Tag;
import com.linkwiki.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class LinkService {

    private final TagService tagService;
    private final LinkRepository linkRepository;
    private final LinkHasTagRepository linkHasTagRepository;
    private final MemberRepository memberRepository;
    private final CategoryTagRepository categoryTagRepository;

    // 링크 최초 등록
    public Long createLink(final Long memberId, final LinkCreateRequest linkCreateRequest) {
        // 1. 멤버 체크
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new InvalidException(ErrorCode.INVALID_REQUEST));
        // 2. 링크 중복 체크
        if (linkRepository.existsByUrl(linkCreateRequest.getUrl())) {
            throw new InvalidException(ErrorCode.DUPLICATE_LINK_URL);
        }
        // 3. 메인 카테고리 검증
        CategoryTag categoryTag = categoryTagRepository.findById(linkCreateRequest.getCategoryTagId())
                .orElseThrow(() -> new InvalidException(ErrorCode.INVALID_REQUEST));
        // 4. 태그 생성
        List<Tag> tags = tagService.createTags(linkCreateRequest.getTags());
        // 5. 링크 생성
        Link link = linkRepository.save(new Link(
                member,
                categoryTag,
                linkCreateRequest.getUrl(),
                linkCreateRequest.getDescription()
        ));
        // 6. 링크has태그 생성
        List<LinkHasTag> linkHasTags = tags.stream()
                .map(tag -> new LinkHasTag(link, tag))
                .toList();
        linkHasTagRepository.saveAll(linkHasTags);

        return link.getId();
    }

    // 링크 조회
    @Transactional(readOnly = true)
    public LinksResponse getLinksByTags(final LinkSearchRequest linkSearchRequest) {
        /**
         * TODO : - 추후 회원 유저의 조회 경우도 구현
         *        - 페이징 구현
         */

        List<Link> linksByTagIds = null;
        // 카테고리가 전체인 경우
        if (linkSearchRequest.getCategoryTagId() == 0) {
            linksByTagIds = linkHasTagRepository.findLinksByTagIds(linkSearchRequest.getTagId(), LinkState.ACTIVE);
        } else {
            // 카테고리가 전체가 아닌 경우
            // 3. 메인 카테고리 검증
            CategoryTag categoryTag = categoryTagRepository.findById(linkSearchRequest.getCategoryTagId())
                    .orElseThrow(() -> new InvalidException(ErrorCode.INVALID_REQUEST));

            linksByTagIds = linkHasTagRepository.findLinksByTagIds(categoryTag, linkSearchRequest.getTagId(), LinkState.ACTIVE);
        }
        return LinksResponse.of(linksByTagIds);
    }
}
