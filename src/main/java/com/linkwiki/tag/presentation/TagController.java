package com.linkwiki.tag.presentation;

import com.linkwiki.tag.dto.response.TagsResponse;
import com.linkwiki.tag.service.TagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @GetMapping("/tag/autocomplete")
    public ResponseEntity<TagsResponse> autoComplete(
            @RequestParam final String keyword
    ) {
        return ResponseEntity.ok().body(tagService.getTagsByKeyword(keyword));
    }
}
