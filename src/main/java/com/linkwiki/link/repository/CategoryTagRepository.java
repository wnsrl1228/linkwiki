package com.linkwiki.link.repository;

import com.linkwiki.link.domain.CategoryTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryTagRepository extends JpaRepository<CategoryTag, Long> {
}
