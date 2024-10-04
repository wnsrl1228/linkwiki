package com.linkwiki.tag.dto;

import com.linkwiki.tag.domain.Tag;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class TagElement {

    private Long id;
    private String name;

    public static TagElement of(Tag tag) {
        return TagElement.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
