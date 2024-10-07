package com.linkwiki.link.presentation;

import com.linkwiki.auth.Admin;
import com.linkwiki.auth.dto.AdminMember;
import com.linkwiki.link.dto.LinkElement;
import com.linkwiki.link.dto.request.LinkModifyRequest;
import com.linkwiki.link.dto.request.LinkReviewRequest;
import com.linkwiki.link.dto.response.LinksResponse;
import com.linkwiki.link.service.LinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RestController
@RequiredArgsConstructor
public class AdminLinkController {

    private final LinkService linkService;

    // 검토 대기중인 링크 리스트 조회
    @GetMapping("/admin/links")
    public ResponseEntity<LinksResponse> getLinksInReviewState(
            @Admin final AdminMember adminMember,
            @PageableDefault(sort = {"createdAt"}, direction = DESC) final Pageable pageable

    ) {
        return ResponseEntity.ok().body(linkService.getLinksInReviewState(pageable));
    }
    // 검토 중인 링크 등록
    @PostMapping("/admin/links/{linkId}")
    public ResponseEntity<Void> reviewLink(
            @Admin final AdminMember adminMember,
            @PathVariable final Long linkId,
            @RequestBody @Valid final LinkReviewRequest linkReviewRequest
    ) {
        linkService.reviewLink(linkId, linkReviewRequest);
        return ResponseEntity.ok().build();
    }
    // 검토 중인 링크 수정 페이지
    @GetMapping("/admin/links/{linkId}")
    public ResponseEntity<LinkElement> getLinkForModification(
            @Admin final AdminMember adminMember,
            @PathVariable final Long linkId
    ) {

        return ResponseEntity.ok().body(linkService.getLinkById(linkId));
    }

    // 검토 중인 링크 수정
    @PatchMapping("/admin/links/{linkId}")
    public ResponseEntity<Void> modifyLink(
            @Admin final AdminMember adminMember,
            @PathVariable final Long linkId,
            @RequestBody @Valid final LinkModifyRequest linkModifyRequest
    ) {
        linkService.modifyLink(linkId, linkModifyRequest);
        return ResponseEntity.ok().build();
    }
}
