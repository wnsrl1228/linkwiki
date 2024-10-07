package com.linkwiki.link.repository;

import com.linkwiki.link.domain.CategoryTag;
import com.linkwiki.link.domain.Link;
import com.linkwiki.link.domain.LinkState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LinkCustomRepository {

    Page<Link> findLinksByTagIds(List<Long> tagIds, Pageable pageable);
    Page<Link> findLinksByTagIds(CategoryTag categoryTag, List<Long> tagIds, Pageable pageable);
}
