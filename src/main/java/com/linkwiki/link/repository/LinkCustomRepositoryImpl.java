package com.linkwiki.link.repository;

import com.linkwiki.link.domain.*;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class LinkCustomRepositoryImpl implements LinkCustomRepository{

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Link> findLinksByTagIds(List<Long> tagIds, Pageable pageable) {
        return findLinksByTagIdsAndCategoryTag(null, tagIds, pageable);
    }

    @Override
    public Page<Link> findLinksByTagIds(CategoryTag categoryTag, List<Long> tagIds, Pageable pageable) {
        return findLinksByTagIdsAndCategoryTag(categoryTag, tagIds, pageable);
    }

    private Page<Link> findLinksByTagIdsAndCategoryTag(CategoryTag categoryTag, List<Long> tagIds, Pageable pageable) {
        QLink link = QLink.link;
        QLinkHasTag linkHasTag = QLinkHasTag.linkHasTag;

        // where절 조건
        BooleanExpression condition = link.state.eq(LinkState.ACTIVE)
                .and(linkHasTag.tag.id.in(tagIds));

        // 카테고리 태그가 null이 아닌 경우 추가 조건
        if (categoryTag != null) {
            condition = condition.and(link.categoryTag.eq(categoryTag));
        }

        List<Link> lists = queryFactory
                .selectFrom(link)
                .leftJoin(link.linkHasTags, linkHasTag)
                .join(link.member).fetchJoin()
                .join(link.categoryTag).fetchJoin()
                .where(condition)
                .groupBy(linkHasTag.link)
                .having(linkHasTag.tag.count().eq(Long.valueOf(tagIds.size())))
                .offset(pageable.getOffset())
                .orderBy(getOrderSpecifier(pageable.getSort()).toArray(OrderSpecifier[]::new))
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(link.count())
                .from(link)
                .where(condition);

        return PageableExecutionUtils.getPage(lists, pageable, countQuery::fetchOne);
    }

    private List<OrderSpecifier> getOrderSpecifier(Sort sort) {
        return sort.stream()
                .map(order -> {
                    Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                    PathBuilder<Link> orderByExpression = new PathBuilder<>(Link.class, "link");
                    return new OrderSpecifier(direction, orderByExpression.get(order.getProperty()));
                })
                .toList();
    }
}
