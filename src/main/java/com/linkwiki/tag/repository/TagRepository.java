package com.linkwiki.tag.repository;

import com.linkwiki.tag.domain.Tag;
import com.linkwiki.tag.domain.TagState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String tag);

    List<Tag> findByNameIn(List<String> tags);

    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:keyword% AND t.state = :state")
    List<Tag> findTagsByKeyword(String keyword, TagState state, Pageable pageable);

    Optional<Tag> findByName(String tagName);
}
