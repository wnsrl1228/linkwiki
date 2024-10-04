package com.linkwiki.link.dto.response;

import com.linkwiki.link.domain.Link;
import com.linkwiki.link.dto.LinkElement;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class LinksResponse {

    private List<LinkElement> links;

    public static LinksResponse of(List<Link> links) {

        List<LinkElement> linkElements = links.stream()
                .map(LinkElement::of)
                .toList();

        return LinksResponse.builder()
                .links(linkElements)
                .build();
    }
}
