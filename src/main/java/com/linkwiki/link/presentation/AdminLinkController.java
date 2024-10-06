package com.linkwiki.link.presentation;

import com.linkwiki.auth.Admin;
import com.linkwiki.auth.dto.AdminMember;
import com.linkwiki.link.dto.request.LinkModifyRequest;
import com.linkwiki.link.dto.request.LinkReviewRequest;
import com.linkwiki.link.dto.response.LinksResponse;
import com.linkwiki.link.service.LinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AdminLinkController {

    private final LinkService linkService;

    // 검토 대기중인 링크 리스트 조회
    @GetMapping("/admin/links")
    public ResponseEntity<LinksResponse> getLinksInReviewState(
            @Admin final AdminMember adminMember
    ) {
        return ResponseEntity.ok().body(linkService.getLinksInReviewState());
    }
    // 등록 api
    @PostMapping("/admin/links/{linkId}")
    public ResponseEntity<Void> reviewLink(
            @Admin final AdminMember adminMember,
            @PathVariable final Long linkId,
            @RequestBody @Valid final LinkReviewRequest linkReviewRequest
    ) {
        linkService.reviewLink(linkId, linkReviewRequest);
        return ResponseEntity.ok().build();
    }
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
