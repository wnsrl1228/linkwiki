package com.linkwiki.link.dto;

import com.linkwiki.Member.dto.response.MemberResponse;
import com.linkwiki.link.domain.Link;
import com.linkwiki.link.domain.LinkHasTag;
import com.linkwiki.tag.dto.TagElement;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class LinkElement {

    private Long id;
    private MemberResponse member;
    private String categoryTag;
    private String url;
    private String description;
    private int likeCount;
    private int bookmarkCount;
    private int clickCount;
    private float rating;
    private List<TagElement> tags;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static LinkElement of(Link link) {

        List<TagElement> tags = new ArrayList<>();
        for (LinkHasTag linkHasTag : link.getLinkHasTags()) {
            tags.add(TagElement.of(linkHasTag.getTag()));
        }

        return LinkElement.builder()
                .id(link.getId())
                .member(MemberResponse.of(link.getMember()))
                .categoryTag(link.getCategoryTag().getName())
                .url(link.getUrl())
                .description(link.getDescription())
                .likeCount(link.getLikeCount())
                .bookmarkCount(link.getBookmarkCount())
                .clickCount(link.getClickCount())
                .rating(link.getRating())
                .tags(tags)
                .createdAt(link.getCreatedAt())
                .updatedAt(link.getUpdatedAt())
                .build();
    }
}
