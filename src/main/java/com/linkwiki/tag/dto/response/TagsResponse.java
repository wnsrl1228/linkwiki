package com.linkwiki.tag.dto.response;

import com.linkwiki.tag.domain.Tag;
import com.linkwiki.tag.dto.TagElement;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class TagsResponse {

    private List<TagElement> tags;

    public static TagsResponse of(List<Tag> tags) {
        List<TagElement> tagElements = tags.stream()
                .map(TagElement::of)
                .toList();

        return TagsResponse.builder()
                .tags(tagElements)
                .build();
    }
}
