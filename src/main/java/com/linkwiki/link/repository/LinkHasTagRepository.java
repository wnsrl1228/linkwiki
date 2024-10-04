package com.linkwiki.link.repository;

import com.linkwiki.link.domain.CategoryTag;
import com.linkwiki.link.domain.Link;
import com.linkwiki.link.domain.LinkHasTag;
import com.linkwiki.link.domain.LinkState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinkHasTagRepository extends JpaRepository<LinkHasTag, Long> {

    @Query("SELECT lh.link FROM LinkHasTag lh WHERE lh.tag.id IN :tagIds AND lh.link.state = :state")
    List<Link> findLinksByTagIds(@Param("tagIds") List<Long> tagIds, @Param("state") LinkState state);

    @Query("""
        SELECT lh.link FROM LinkHasTag lh 
        WHERE lh.tag.id IN :tagIds AND lh.link.categoryTag = :categoryTag AND lh.link.state = :state 
    """)
    List<Link> findLinksByTagIds(@Param("categoryTag") CategoryTag categoryTag, @Param("tagIds") List<Long> tagIds, @Param("state") LinkState state);
}
