package com.linkwiki.link.repository;

import com.linkwiki.link.domain.Link;
import com.linkwiki.link.domain.LinkState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LinkRepository extends JpaRepository<Link, Long> {

    boolean existsByUrl(String url);

    List<Link> findByState(LinkState review);
}
