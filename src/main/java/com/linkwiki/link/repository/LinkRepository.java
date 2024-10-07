package com.linkwiki.link.repository;

import com.linkwiki.link.domain.Link;
import com.linkwiki.link.domain.LinkState;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LinkRepository extends JpaRepository<Link, Long> , LinkCustomRepository{

    boolean existsByUrl(String url);

    Page<Link> findByState(LinkState review, Pageable pageable);

}
