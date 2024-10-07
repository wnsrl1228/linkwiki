package com.linkwiki.link.dto.response;

import com.linkwiki.link.domain.Link;
import com.linkwiki.link.dto.LinkElement;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
public class LinksResponse {

    private List<LinkElement> links;
    private Integer page;
    private Integer totalPages;
    private Long totalElement;
    private Boolean isFirst;
    private Boolean isLast;
    private Boolean isEmpty;

    public static LinksResponse of(Page<Link> links) {

        List<LinkElement> linkElements = links.stream()
                .map(LinkElement::of)
                .toList();

        return LinksResponse.builder()
                .links(linkElements)
                .page(links.getNumber())
                .totalPages(links.getTotalPages())
                .totalElement(links.getTotalElements())
                .isFirst(links.isFirst())
                .isLast(links.isLast())
                .isEmpty(links.isEmpty())
                .build();
    }
}
