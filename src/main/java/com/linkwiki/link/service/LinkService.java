package com.linkwiki.link.service;

import com.linkwiki.Member.domain.Member;
import com.linkwiki.Member.repository.MemberRepository;
import com.linkwiki.global.exception.ErrorCode;
import com.linkwiki.global.exception.InvalidException;
import com.linkwiki.link.domain.*;
import com.linkwiki.link.dto.request.LinkCreateRequest;
import com.linkwiki.link.dto.request.LinkModifyRequest;
import com.linkwiki.link.dto.request.LinkReviewRequest;
import com.linkwiki.link.dto.request.LinkSearchRequest;
import com.linkwiki.link.dto.response.LinksResponse;
import com.linkwiki.link.repository.CategoryTagRepository;
import com.linkwiki.link.repository.LinkHasTagRepository;
import com.linkwiki.link.repository.LinkRepository;
import com.linkwiki.link.repository.LinkResultRepository;
import com.linkwiki.tag.domain.Tag;
import com.linkwiki.tag.domain.TagState;
import com.linkwiki.tag.repository.TagRepository;
import com.linkwiki.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final LinkResultRepository linkResultRepository;
    private final TagRepository tagRepository;

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

    // 운영자 전용 : 검토 대기중인 링크 조회
    @Transactional(readOnly = true)
    public LinksResponse getLinksInReviewState() {
        List<Link> links = linkRepository.findByState(LinkState.REVIEW);
        return LinksResponse.of(links);
    }

    // 운영자 전용 : 링크 검토
    public void reviewLink(final Long linkId, final LinkReviewRequest linkReviewRequest) {
        Link link = linkRepository.findById(linkId)
                .orElseThrow(() -> new InvalidException(ErrorCode.INVALID_REQUEST));

        linkResultRepository.save(new LinkResult(link, linkReviewRequest.getReason()));

        if (linkReviewRequest.getResult()) {
            link.changeState(LinkState.ACTIVE);
            for (LinkHasTag linkHasTag : link.getLinkHasTags()) {
                linkHasTag.getTag().changeState(TagState.ACTIVE);
            }
            return;
        }

        link.changeState(LinkState.REJECTED);
    }

    // 운영자 전용 : 링크 수정
    public void modifyLink(Long linkId, LinkModifyRequest linkModifyRequest) {
        /**
         * 기존 등록된 태그와 수정된 태그를 비교후 교체해줘야됨
         *
         * 신규 태그 for문 돌면서
         *      if 기존 태그에 이미 있는 경우
         *          삭제할 태그 리스트에서 제거
         *      else 기존 태그에 없는 경우
         *          새로 추가할 태그 리스트에 추가
         * 최종적으로
         *  삭제할 태그 리스트의 linkHasTag삭제
         *  추가할 태그 리스트의 linkHasTag추가
         *
         */

        Link link = linkRepository.findById(linkId)
                .orElseThrow(() -> new InvalidException(ErrorCode.INVALID_REQUEST));

        List<LinkHasTag> tagsToRemove = link.getLinkHasTags();
        List<Tag> newTags = new ArrayList<>();

        for (String tagName : linkModifyRequest.getTags()) {

            // 기존 태그인 경우
            boolean result = tagsToRemove.removeIf(
                    linkHasTag -> linkHasTag.getTag().getName().equals(tagName)
            );

            // 수정된 태그인 경우
            if (!result) {
                // 새로운 태그인지 체크
                Tag tag = tagRepository.findByName(tagName).orElse(null);

                // 새로운 태그인 경우
                if (tag == null) {
                    tag = tagRepository.save(new Tag(tagName));
                }
                newTags.add(tag);
            }
        }

        // 최종 태그연결 수정
        linkHasTagRepository.deleteAll(tagsToRemove);
        for (Tag newTag : newTags) {
            linkHasTagRepository.save(new LinkHasTag(link, newTag));
        }
    }
}
