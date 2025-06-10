package com.duri.domain.post.event;

import com.duri.domain.couple.entity.Couple;
import com.duri.domain.notification.constant.NotificationType;
import com.duri.domain.notification.entity.Notification;
import com.duri.domain.notification.service.NotificationService;
import com.duri.domain.post.entity.Post;
import com.duri.domain.post.service.PostStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostEventListener {

    private final PostStatService postStatService;
    private final NotificationService notificationService;

    // 외부 알림 서비스 (FCM) 연동되면 @Async 고려

    @EventListener
    public void handlePostCreated(PostCreatedEvent event) {
        Post post = event.getPost();
        Couple couple = post.getCouple();
        boolean isLeftUser = (post.getUserLeftRate() != null);

        Notification notification = Notification.builder()
            .type(NotificationType.POST)
            .to(isLeftUser ? couple.getUserRight() : couple.getUserLeft())
            .from(isLeftUser ? couple.getUserLeft() : couple.getUserRight())
            .content(post.getTitle() + " 게시글 작성을 완료해주세요")
            .build();

        notificationService.send(notification);
    }


    @EventListener
    public void handlePostLiked(PostLikedEvent event) {
        Post post = event.getPost();
        Couple couple = event.getCouple();

        postStatService.increaseLikeCount(post.getId());

        Notification notificationLeft = Notification.builder()
            .type(NotificationType.LIKE)
            .to(post.getCouple().getUserLeft())
            .fromCouple(couple)
            .content(couple.getName() + " 이 좋아요를 눌렀습니다")
            .build();

        Notification notificationRight = Notification.builder()
            .type(NotificationType.LIKE)
            .to(post.getCouple().getUserRight())
            .fromCouple(couple)
            .content(couple.getName() + " 이 좋아요를 눌렀습니다")
            .build();

        notificationService.send(notificationLeft);
        notificationService.send(notificationRight);
    }
}
