package com.duri.domain.post.repository;


import static com.duri.domain.couple.entity.QCouple.couple;
import static com.duri.domain.post.constant.search.PostSortDirection.ASC;
import static com.duri.domain.post.entity.QPost.post;
import static com.duri.domain.user.entity.QUser.user;
import static org.springframework.util.StringUtils.hasText;

import com.duri.domain.post.constant.PostStatus;
import com.duri.domain.post.constant.Scope;
import com.duri.domain.post.constant.search.PostSortBy;
import com.duri.domain.post.constant.search.PostSortDirection;
import com.duri.domain.post.dto.PostCursor;
import com.duri.domain.post.dto.PostSearchOptions;
import com.duri.domain.post.entity.Post;
import com.duri.domain.user.entity.QUser;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class SearchPostRepositoryImpl implements SearchPostRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Post> findCompletePostsBySearchOptions(PostCursor cursor, int size,
        PostSearchOptions searchOptions) {
        QUser user2 = new QUser("user2");

        return queryFactory
            .selectFrom(post)
            .leftJoin(post.couple, couple).fetchJoin()
            .leftJoin(couple.userLeft, user).fetchJoin()
            .leftJoin(couple.userRight, user2).fetchJoin()
            .where(
                cursorDirection(cursor, searchOptions.getSortDirection(),
                    searchOptions.getSortBy()),
                searchKeywordContains(searchOptions.getSearchKeyword()),
                createdDateBetween(searchOptions.getStartDate(), searchOptions.getEndDate()),
                post.status.eq(PostStatus.COMPLETE),
                post.scope.eq(Scope.PUBLIC)
            )
            .orderBy(getOrderSpecifier(searchOptions.getSortBy(), searchOptions.getSortDirection()))
            .limit(size)
            .fetch();
    }

    @Override
    public List<Post> findCompletePostsBySearchOptions(PostCursor cursor, int size,
        PostSearchOptions searchOptions, String coupleCode) {
        QUser user2 = new QUser("user2");

        return queryFactory
            .selectFrom(post)
            .leftJoin(post.couple, couple).fetchJoin()
            .leftJoin(couple.userLeft, user).fetchJoin()
            .leftJoin(couple.userRight, user2).fetchJoin()
            .where(
                cursorDirection(cursor, searchOptions.getSortDirection(),
                    searchOptions.getSortBy()),
                searchKeywordContains(searchOptions.getSearchKeyword()),
                createdDateBetween(searchOptions.getStartDate(), searchOptions.getEndDate()),
                post.status.eq(PostStatus.COMPLETE),
                post.couple.code.eq(coupleCode)
            )
            .orderBy(getOrderSpecifier(searchOptions.getSortBy(), searchOptions.getSortDirection()))
            .limit(size)
            .fetch();
    }

    @Override
    public List<Post> findPendingPostsBySearchOptions(PostCursor cursor, int size,
        PostSearchOptions searchOptions, String coupleCode) {
        QUser user2 = new QUser("user2");

        return queryFactory
            .selectFrom(post)
            .leftJoin(post.couple, couple).fetchJoin()
            .leftJoin(couple.userLeft, user).fetchJoin()
            .leftJoin(couple.userRight, user2).fetchJoin()
            .where(
                cursorDirection(cursor, searchOptions.getSortDirection(),
                    searchOptions.getSortBy()),
                searchKeywordContains(searchOptions.getSearchKeyword()),
                createdDateBetween(searchOptions.getStartDate(), searchOptions.getEndDate()),
                post.status.eq(PostStatus.PENDING),
                post.couple.code.eq(coupleCode)
            )
            .orderBy(getOrderSpecifier(searchOptions.getSortBy(), searchOptions.getSortDirection()))
            .limit(size)
            .fetch();
    }

    private BooleanExpression cursorDirection(PostCursor cursor, PostSortDirection sortDirection,
        PostSortBy sortBy) {
        if (cursor.getId() == null) {
            return null;
        }
        boolean isAsc = sortDirection == ASC;
        return switch (sortBy) {
            case RATE -> {
                if (cursor.getDate() == null) {
                    // rate가 같고 date가 null인 데이터는 ID 기준으로 정렬
                    yield post.rate.lt(cursor.getRate())
                        .or(
                            post.rate.eq(cursor.getRate())
                                .and(post.date.isNull())
                                .and(post.id.lt(cursor.getId()))
                        );
                } else {
                    // rate 기준 정렬
                    BooleanExpression rateCmp = isAsc
                        ? post.rate.gt(cursor.getRate())
                        : post.rate.lt(cursor.getRate());

                    // rate 같고, date 기준 (날짜는 최신순 고정 = DESC)
                    BooleanExpression dateCmp = post.rate.eq(cursor.getRate())
                        .and(post.date.isNotNull())
                        .and(post.date.lt(cursor.getDate()));

                    // rate, date 같고, id 기준
                    BooleanExpression idCmp = post.rate.eq(cursor.getRate())
                        .and(post.date.eq(cursor.getDate()))
                        .and(post.id.lt(cursor.getId()));

                    yield rateCmp.or(dateCmp).or(idCmp);
                }
            }
            default -> {
                if (cursor.getDate() == null) {
                    // 날짜가 없는 데이터만 가져오기 (ID 기준)
                    yield post.date.isNull().and(post.id.lt(cursor.getId()));
                } else {
                    // 날짜가 있는 데이터는 날짜 내림차순 + ID
                    // 날짜가 없는 데이터는 ID 기준
                    yield post.date.isNotNull()
                        .and(
                            post.date.lt(cursor.getDate())
                                .or(post.date.eq(cursor.getDate()).and(post.id.lt(cursor.getId())))
                        )
                        .or(
                            post.date.isNull().and(post.id.lt(cursor.getId()))
                        );
                }
            }
        };
    }

    private OrderSpecifier<?>[] getOrderSpecifier(PostSortBy sortBy,
        PostSortDirection sortDirection) {
        return switch (sortBy) {
            case RATE -> sortDirection == ASC
                ? new OrderSpecifier[]{post.rate.asc(), post.date.desc(), post.id.desc()}
                : new OrderSpecifier[]{post.rate.desc(), post.date.desc(), post.id.desc()};
            // 추가 예정
            default -> sortDirection == ASC
                ? new OrderSpecifier[]{post.date.asc(), post.id.desc()}
                : new OrderSpecifier[]{post.date.desc(), post.id.desc()};
        };
    }

    private BooleanExpression createdDateBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return null;
        }

        if (startDate != null && endDate != null) {
            return post.createdAt.between(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            );
        }

        if (startDate != null) {
            return post.createdAt.goe(startDate.atStartOfDay());
        }

        return post.createdAt.lt(endDate.plusDays(1).atStartOfDay());
    }

    private BooleanExpression searchKeywordContains(String searchKeyword) {
        if (!hasText(searchKeyword)) {
            return null;
        }
        return post.title.containsIgnoreCase(searchKeyword)
            .or(post.address.containsIgnoreCase(searchKeyword))
            .or(post.placeName.containsIgnoreCase(searchKeyword))
            .or(post.category.containsIgnoreCase(searchKeyword));
    }
}
