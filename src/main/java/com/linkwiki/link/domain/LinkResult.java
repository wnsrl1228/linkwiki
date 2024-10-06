package com.linkwiki.link.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class LinkResult {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // LinkResult의 id를 Link의 id와 매핑
    @JoinColumn(name = "id") // Link의 id와 매핑
    private Link link;

    @Column
    private String reason;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public LinkResult(Link link, String reason) {
        this.link = link;
        this.reason = reason;
    }
}
