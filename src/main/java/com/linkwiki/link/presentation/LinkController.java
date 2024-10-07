package com.linkwiki.link.presentation;

import com.linkwiki.auth.Login;
import com.linkwiki.auth.dto.LoginMember;
import com.linkwiki.link.dto.request.LinkCreateRequest;
import com.linkwiki.link.dto.request.LinkSearchRequest;
import com.linkwiki.link.dto.response.LinksResponse;
import com.linkwiki.link.service.LinkService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import static org.springframework.data.domain.Sort.Direction.DESC;

@RequiredArgsConstructor
@RestController
public class LinkController {

    private final LinkService linkService;

    @PostMapping("/links")
    public ResponseEntity<Void> createLink(
            @Login final LoginMember member,
            @RequestBody @Valid final LinkCreateRequest linkCreateRequest
    ) {
        linkService.createLink(member.getMemberId(), linkCreateRequest);
        return ResponseEntity.created(URI.create("/mypage/links/status")).build();
    }

    @GetMapping("/links/search")
    public ResponseEntity<LinksResponse> search(
            @ModelAttribute @Valid LinkSearchRequest linkSearchRequest,
            @PageableDefault(sort = {"createdAt", "likeCount", "clickCount", "rating"}, direction = DESC) final Pageable pageable
    ) {
        return ResponseEntity.ok().body(linkService.getLinksByTags(linkSearchRequest, pageable));
    }

}
