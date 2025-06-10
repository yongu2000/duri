package com.duri.domain.notification.repository;

import static com.duri.domain.notification.entity.QNotification.notification;
import static com.duri.domain.user.entity.QUser.user;

import com.duri.domain.notification.dto.NotificationCursor;
import com.duri.domain.notification.entity.Notification;
import com.duri.domain.user.entity.QUser;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class NotificationSearchRepositoryImpl implements NotificationSearchRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Notification> findUnconfirmedNotifications(NotificationCursor cursor, int limit,
        Long userId) {
        QUser user2 = new QUser("user2");

        return queryFactory
            .selectFrom(notification)
            .leftJoin(notification.to, user)
            .leftJoin(notification.from, user2)
            .where(
                cursorDirection(cursor),
                notification.confirmed.eq(false),
                notification.to.id.eq(userId)
            )
            .orderBy(getOrderSpecifier())
            .limit(limit)
            .fetch();
    }

    @Override
    public List<Notification> findConfirmedNotifications(NotificationCursor cursor, int limit,
        Long userId) {
        QUser user2 = new QUser("user2");

        return queryFactory
            .selectFrom(notification)
            .leftJoin(notification.to, user)
            .leftJoin(notification.from, user2)
            .where(
                cursorDirection(cursor),
                notification.confirmed.eq(true),
                notification.to.id.eq(userId)
            )
            .orderBy(getOrderSpecifier())
            .limit(limit)
            .fetch();
    }

    private BooleanExpression cursorDirection(NotificationCursor cursor) {
        if (cursor.getDate() == null || cursor.getId() == null) {
            return null; // 커서 없으면 전체 조회
        }
        return notification.createdAt.lt(cursor.getDate())
            .or(
                notification.createdAt.eq(cursor.getDate())
                    .and(notification.id.lt(cursor.getId()))
            );
    }

    private OrderSpecifier<?>[] getOrderSpecifier() {
        return new OrderSpecifier[]{notification.createdAt.desc(), notification.id.desc()};
    }
}
