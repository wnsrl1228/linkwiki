package com.linkwiki.tag.repository;

import com.linkwiki.tag.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    boolean existsByName(String tag);

    List<Tag> findByNameIn(List<String> tags);
}
