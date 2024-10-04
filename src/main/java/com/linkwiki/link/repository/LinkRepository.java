package com.linkwiki.link.repository;

import com.linkwiki.link.domain.Link;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkRepository extends JpaRepository<Link, Long> {

    boolean existsByUrl(String url);
}
