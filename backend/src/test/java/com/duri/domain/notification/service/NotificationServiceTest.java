package com.duri.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.duri.domain.notification.constant.NotificationType;
import com.duri.domain.notification.dto.AllNotificationResponse;
import com.duri.domain.notification.dto.NotificationCursor;
import com.duri.domain.notification.dto.NotificationResponse;
import com.duri.domain.notification.entity.Notification;
import com.duri.domain.notification.exception.NotificationNotFoundException;
import com.duri.domain.notification.repository.NotificationRepository;
import com.duri.domain.sse.RedisMessagePublisher;
import com.duri.domain.user.entity.User;
import com.duri.global.dto.CursorResponse;
import com.duri.global.util.AESUtilTestHelper;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 서비스 단위 테스트")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private RedisMessagePublisher publisher;

    @InjectMocks
    private NotificationService notificationService;

    private User toUser;
    private User fromUser;
    private Notification notification;

    @BeforeEach
    void setUp() {
        toUser = User.builder()
            .id(1L)
            .username("toUser")
            .build();
        fromUser = User.builder()
            .id(2L)
            .username("fromUser")
            .build();
        notification = Notification.builder()
            .id(100L)
            .to(toUser)
            .from(fromUser)
            .type(NotificationType.POST)
            .content("알림테스트")
            .confirmed(false)
            .build();
        // createdAt 등 필요한 필드 추가로 세팅 가능
        // 혹시 protected/private면 ReflectionTestUtils.setField 사용 가능
    }

    @Test
    @DisplayName("알림 저장 및 redis publish 정상 동작")
    void send_정상동작() {
        // given
        given(notificationRepository.save(any(Notification.class))).willReturn(notification);

        // when
        notificationService.send(notification);

        // then
        then(notificationRepository).should().save(any(Notification.class));
        then(publisher).should().publishNotification(
            eq("toUser"), eq("POST"), eq("알림테스트")
        );
    }

    @Test
    @DisplayName("전체 알림 조회 정상 동작")
    void getAllNotifications_정상동작() {
        // given
        given(notificationRepository.findByUserId(1L)).willReturn(List.of(notification));

        // when
        AllNotificationResponse resp = notificationService.getAllNotifications(1L);

        // then
        assertThat(resp).isNotNull();
        then(notificationRepository).should().findByUserId(1L);
    }

    @Test
    @DisplayName("미확인 알림 개수 조회 정상 동작")
    void getUnconfirmedNotificationsCount_정상동작() {
        // given
        given(notificationRepository.getUnconfirmedNotificationsCount(1L)).willReturn(2L);

        // when
        Long count = notificationService.getUnconfirmedNotificationsCount(1L);

        // then
        assertThat(count).isEqualTo(2L);
        then(notificationRepository).should().getUnconfirmedNotificationsCount(1L);
    }

    @Test
    @DisplayName("커서 페이지네이션 미확인 알림 조회 + 읽음처리")
    void getUnconfirmedNotifications_정상동작() {
        // given
        NotificationCursor cursor = new NotificationCursor(null, null);
        List<Notification> notifications = List.of(notification);
        given(notificationRepository.findUnconfirmedNotifications(cursor, 2, 1L))
            .willReturn(notifications);

        // when
        CursorResponse<NotificationResponse, NotificationCursor> resp =
            notificationService.getUnconfirmedNotifications(cursor, 1, 1L);

        // then
        assertThat(resp).isNotNull();
        then(notificationRepository).should().findUnconfirmedNotifications(cursor, 2, 1L);
        then(notificationRepository).should().updateConfirmedByIds(anyList());
    }

    @Test
    @DisplayName("커서 페이지네이션 확인된 알림 조회")
    void getConfirmedNotifications_정상동작() {
        // given
        NotificationCursor cursor = new NotificationCursor(null, null);
        List<Notification> notifications = List.of(notification);
        given(notificationRepository.findConfirmedNotifications(cursor, 2, 1L))
            .willReturn(notifications);

        // when
        CursorResponse<NotificationResponse, NotificationCursor> resp =
            notificationService.getConfirmedNotifications(cursor, 1, 1L);

        // then
        assertThat(resp).isNotNull();
        then(notificationRepository).should().findConfirmedNotifications(cursor, 2, 1L);
    }

    @Test
    @DisplayName("알림 단건 삭제 (권한체크 Aspect는 생략, Repository만 verify)")
    void delete_정상동작() {
        // given
        given(notificationRepository.findById(100L)).willReturn(Optional.of(notification));

        // when
        notificationService.delete(100L);

        // then
        then(notificationRepository).should().findById(100L);
        then(notificationRepository).should().delete(notification);
    }

    @Test
    @DisplayName("알림 전체 삭제")
    void deleteAll_정상동작() {
        // when
        notificationService.deleteAll(1L);

        // then
        then(notificationRepository).should().deleteByToId(1L);
    }

    @Test
    @DisplayName("알림 단건 조회 정상 동작")
    void findById_정상동작() {
        // given
        given(notificationRepository.findById(100L)).willReturn(Optional.of(notification));

        // when
        Notification found = notificationService.findById(100L);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(100L);
        then(notificationRepository).should().findById(100L);
    }

    @Test
    @DisplayName("알림 단건 조회 실패 시 예외 발생")
    void findById_예외() {
        // given
        given(notificationRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> notificationService.findById(999L))
            .isInstanceOf(NotificationNotFoundException.class);
        then(notificationRepository).should().findById(999L);
    }

    @BeforeEach
    void AESUtilSetUp() {
        AESUtilTestHelper.setSecretKey("1234567890abcdef");
    }
}