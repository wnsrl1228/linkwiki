package com.linkwiki.tag.repository;

import com.linkwiki.tag.domain.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String tag);

    List<Tag> findByNameIn(List<String> tags);

    @Query("SELECT t FROM Tag t WHERE t.name LIKE %:keyword%")
    List<Tag> findTagsByKeyword(String keyword, Pageable pageable);
}
