package com.duri.domain.post.repository.comment;

import static com.duri.domain.couple.entity.QCouple.couple;
import static com.duri.domain.post.constant.search.SortDirection.ASC;
import static com.duri.domain.post.entity.QComment.comment;
import static com.duri.domain.post.entity.QCommentStat.commentStat;

import com.duri.domain.couple.entity.QCouple;
import com.duri.domain.post.constant.search.CommentSortBy;
import com.duri.domain.post.constant.search.SortDirection;
import com.duri.domain.post.dto.comment.CommentCursorRequestDto;
import com.duri.domain.post.dto.comment.CommentSearchOptions;
import com.duri.domain.post.entity.Comment;
import com.duri.domain.post.entity.QComment;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class SearchCommentRepositoryImpl implements
    SearchCommentRepository {

    private final JPAQueryFactory queryFactory;


    @Override
    public List<Comment> findParentCommentsByPost(CommentCursorRequestDto cursor, int size,
        CommentSearchOptions searchOptions, Long postId) {

        return queryFactory
            .selectFrom(comment)
            .leftJoin(comment.couple, couple).fetchJoin()
            .leftJoin(comment.commentStat, commentStat).fetchJoin()
            .where(
                cursorDirection(cursor),
                comment.post.id.eq(postId),
                comment.parentComment.isNull()
            )
            .orderBy(getOrderSpecifier(searchOptions.getSortBy(), searchOptions.getSortDirection()))
            .limit(size)
            .fetch();
    }

    @Override
    public List<Comment> findCommentRepliesByPost(CommentCursorRequestDto cursor, int size,
        Long parentCommentId) {
        log.info("Repository - findCommentRepliesByPost Started");

        QComment replyTo = new QComment("replyTo");
        QCouple replyToCouple = new QCouple("replyToCouple");

        return queryFactory
            .selectFrom(comment)
            .leftJoin(comment.couple, couple).fetchJoin()
            .leftJoin(comment.replyToComment, replyTo).fetchJoin() // N+1
            .leftJoin(replyTo.couple, replyToCouple).fetchJoin() // N+1
            .where(
                replyCursorDirection(cursor),
                comment.parentComment.id.eq(parentCommentId)
            )
            .orderBy(getOrderSpecifier(CommentSortBy.CREATED_AT, ASC))
            .limit(size)
            .fetch();
    }

    private BooleanExpression cursorDirection(CommentCursorRequestDto cursor) {
        if (cursor.getCreatedAt() == null || cursor.getId() == null) {
            return null;
        }
        return comment.createdAt.lt(cursor.getCreatedAt())
            .or(
                comment.createdAt.eq(cursor.getCreatedAt())
                    .and(comment.id.lt(cursor.getId()))
            );
    }

    private BooleanExpression replyCursorDirection(CommentCursorRequestDto cursor) {
        if (cursor.getCreatedAt() == null || cursor.getId() == null) {
            return null;
        }
        return comment.createdAt.gt(cursor.getCreatedAt())
            .or(
                comment.createdAt.eq(cursor.getCreatedAt())
                    .and(comment.id.gt(cursor.getId()))
            );
    }

    private OrderSpecifier<?>[] getOrderSpecifier(CommentSortBy sortBy,
        SortDirection sortDirection) {
        return switch (sortBy) {
            case CREATED_AT -> sortDirection == ASC
                ? new OrderSpecifier[]{comment.createdAt.asc(), comment.id.desc()}
                : new OrderSpecifier[]{comment.createdAt.desc(), comment.id.desc()};
        };
    }

}
