package com.duri.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.duri.domain.notification.TestNotificationEntityFactory;
import com.duri.domain.notification.constant.NotificationType;
import com.duri.domain.notification.dto.NotificationCursor;
import com.duri.domain.notification.entity.Notification;
import com.duri.domain.user.entity.Gender;
import com.duri.domain.user.entity.Role;
import com.duri.domain.user.entity.User;
import com.duri.domain.user.repository.UserRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({NotificationSearchRepositoryImpl.class})
class NotificationSearchRepositoryImplTest {

    @Autowired
    @Qualifier("notificationSearchRepositoryImpl")
    private NotificationSearchRepository notificationSearchRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private User toUser;
    private User fromUser;

    private Notification n1, n2, n3;

    @BeforeEach
    void setUp() {
        // 고유 이메일, 유저 저장
        toUser = userRepository.save(User.builder()
            .username("toUser")
            .email("toUserEmail@test.com")
            .name("toUserName")
            .password("toUserPassword")
            .role(Role.USER)
            .gender(Gender.MALE)
            .build());
        fromUser = userRepository.save(User.builder()
            .username("fromUser")
            .email("fromUserEmail@test.com")
            .name("fromUserName")
            .password("fromUserPassword")
            .role(Role.USER)
            .gender(Gender.MALE)
            .build());

        // createdAt: 10:00, 09:00, 08:00 / id는 save순으로 자동 증가
        n1 = notificationRepository.save(
            TestNotificationEntityFactory.createNotificationWithCreatedAt(
                toUser, fromUser, "알림1", NotificationType.POST, false,
                LocalDateTime.of(2024, 6, 9, 10, 0)));
        n2 = notificationRepository.save(
            TestNotificationEntityFactory.createNotificationWithCreatedAt(
                toUser, fromUser, "알림2", NotificationType.POST, false,
                LocalDateTime.of(2024, 6, 9, 9, 0)));
        n3 = notificationRepository.save(
            TestNotificationEntityFactory.createNotificationWithCreatedAt(
                toUser, fromUser, "알림3", NotificationType.POST, false,
                LocalDateTime.of(2024, 6, 9, 8, 0)));
    }

    @Test
    @DisplayName("커서 없이 size=2이면 최신순 2개 반환, hasNext=true")
    void 첫페이지_커서없음_size2() {
        NotificationCursor cursor = new NotificationCursor(null, null);
        int size = 2;
        List<Notification> result = notificationSearchRepository.findUnconfirmedNotifications(
            cursor, size + 1, toUser.getId());

        boolean hasNext = result.size() > size;
        List<Notification> page = hasNext ? result.subList(0, size) : result;

        assertThat(page).hasSize(2);
        assertThat(page).allMatch(n -> !n.isConfirmed());
        // 최신순 정렬 확인
        assertThat(page.get(0).getCreatedAt()).isAfter(page.get(1).getCreatedAt());
        assertThat(hasNext).isTrue(); // 데이터 2개 초과 → 다음 페이지 존재
    }

    @Test
    @DisplayName("커서 값 지정: n1(10시, id=...) 다음부터 size=1 → n2만 반환, hasNext=false")
    void 커서_중간지점부터_이어받기() {
        // n1은 10시, n3은 8시, 둘 다 미확인
        NotificationCursor cursor = new NotificationCursor(n1.getCreatedAt(), n1.getId());
        int size = 1;
        List<Notification> result = notificationSearchRepository.findUnconfirmedNotifications(
            cursor, size + 1, toUser.getId());

        boolean hasNext = result.size() > size;
        List<Notification> page = hasNext ? result.subList(0, size) : result;

        assertThat(page).hasSize(1);
        assertThat(page.get(0).getContent()).isEqualTo("알림2");
        assertThat(hasNext).isTrue(); // 다음 페이지 n3
    }

    @Test
    @DisplayName("마지막 커서로 조회 시 결과 없음")
    void 마지막커서_조회() {
        NotificationCursor cursor = new NotificationCursor(n3.getCreatedAt(), n3.getId());
        int size = 2;
        List<Notification> result = notificationSearchRepository.findUnconfirmedNotifications(
            cursor, size + 1, toUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("알림이 1개만 있을 때 정상 동작")
    void 알림1개_동작() {
        notificationRepository.deleteAll(); // 전체 삭제 후 1개만
        Notification only = notificationRepository.save(
            TestNotificationEntityFactory.createNotificationWithCreatedAt(
                toUser, fromUser, "유일알림", NotificationType.POST, false,
                LocalDateTime.of(2024, 6, 9, 11, 0)
            ));

        NotificationCursor cursor = new NotificationCursor(null, null);
        List<Notification> result = notificationSearchRepository.findUnconfirmedNotifications(
            cursor, 2, toUser.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getContent()).isEqualTo("유일알림");
    }

    @Test
    @DisplayName("알림이 없는 경우 정상 동작")
    void 알림없음_동작() {
        notificationRepository.deleteAll();
        NotificationCursor cursor = new NotificationCursor(null, null);
        List<Notification> result = notificationSearchRepository.findUnconfirmedNotifications(
            cursor, 2, toUser.getId());
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("확인된 알림만 필터링")
    void 확인알림만_조회() {
        NotificationCursor cursor = new NotificationCursor(null, null);
        List<Notification> confirmed = notificationSearchRepository.findConfirmedNotifications(
            cursor, 2, toUser.getId());

        assertThat(confirmed).allMatch(Notification::isConfirmed);
    }

    @Test
    @DisplayName("같은 createdAt에서 id 내림차순 동작")
    void createdAt_동일_id내림차순() {
        // 같은 시간, id만 다르게 2개 생성
        Notification nA = notificationRepository.save(
            TestNotificationEntityFactory.createNotificationWithCreatedAt(
                toUser, fromUser, "동시간1", NotificationType.POST, false,
                LocalDateTime.of(2024, 6, 9, 12, 0)
            ));
        Notification nB = notificationRepository.save(
            TestNotificationEntityFactory.createNotificationWithCreatedAt(
                toUser, fromUser, "동시간2", NotificationType.POST, false,
                LocalDateTime.of(2024, 6, 9, 12, 0)
            ));

        NotificationCursor cursor = new NotificationCursor(null, null);
        List<Notification> result = notificationSearchRepository.findUnconfirmedNotifications(
            cursor, 2, toUser.getId());

        // id 내림차순이므로, 더 큰 id가 먼저 나옴
        assertThat(result.get(0).getId()).isGreaterThan(result.get(1).getId());
    }

    @TestConfiguration
    static class QuerydslTestConfig {

        @Bean
        public JPAQueryFactory jpaQueryFactory(EntityManager em) {
            return new JPAQueryFactory(em);
        }
    }
}