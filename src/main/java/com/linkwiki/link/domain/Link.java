package com.linkwiki.link.domain;

import com.linkwiki.Member.domain.Member;
import com.linkwiki.link.dto.request.LinkCreateRequest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_tag_id")
    private CategoryTag categoryTag;

    @BatchSize(size = 20)
    @OneToMany(mappedBy = "link")
    private List<LinkHasTag> linkHasTags = new ArrayList<>();

    @Column(nullable = false, unique = true)
    private String url;

    @Column(nullable = false, length = 100)
    private String description;

    @Column(nullable = false)
    private int likeCount;

    @Column(nullable = false)
    private int bookmarkCount;

    @Column(nullable = false)
    private int clickCount;

    @Column(nullable = false)
    private float rating;

    @Enumerated(value = EnumType.STRING)
    private LinkState state;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public Link(Member member, CategoryTag categoryTag, String url, String description) {
        this.member = member;
        this.categoryTag = categoryTag;
        this.url = url;
        this.description = description;
        state = LinkState.REVIEW;
    }

    public void changeState(LinkState state) {
        this.state = state;
    }
}
