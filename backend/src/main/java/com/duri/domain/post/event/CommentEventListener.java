package com.duri.domain.post.event;

import com.duri.domain.notification.constant.NotificationType;
import com.duri.domain.notification.entity.Notification;
import com.duri.domain.notification.service.NotificationService;
import com.duri.domain.post.entity.Comment;
import com.duri.domain.post.entity.Post;
import com.duri.domain.post.service.CommentStatService;
import com.duri.domain.post.service.PostStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.REQUIRES_NEW)
public class CommentEventListener {

    private final PostStatService postStatService;
    private final CommentStatService commentStatService;
    private final NotificationService notificationService;

    @TransactionalEventListener
    public void handleCommentCreated(CommentCreatedEvent event) {
        Comment comment = event.getComment();
        Post post = event.getPost();

        postStatService.increaseCommentCount(post.getId());

        String content = post.getTitle() + " 게시글에" + comment.getCouple().getName() + "가 댓글을 달았습니다. "
            + comment.getContent();

        Notification notificationLeft = Notification.builder()
            .type(NotificationType.COMMENT)
            .to(post.getCouple().getUserLeft())
            .fromCouple(comment.getCouple())
            .content(content)
            .build();
        Notification notificationRight = Notification.builder()
            .type(NotificationType.COMMENT)
            .to(post.getCouple().getUserRight())
            .fromCouple(comment.getCouple())
            .content(content)
            .build();

        notificationService.send(notificationLeft);
        notificationService.send(notificationRight);
    }


    @EventListener
    public void handleCommentReplyCreated(CommentReplyCreatedEvent event) {
        Comment comment = event.getComment();
        Comment parentComment = event.getParentComment();
        Comment replyTo = event.getReplyTo();
        Post post = event.getPost();

        commentStatService.increaseCommentCount(parentComment.getId());
        postStatService.increaseCommentCount(post.getId());

        String content =
            replyTo.getContent() + " 댓글에" + comment.getCouple().getName() + "가 답글을 달았습니다. "
                + comment.getContent();

        Notification notificationLeft = Notification.builder()
            .type(NotificationType.COMMENT)
            .to(replyTo.getCouple().getUserLeft())
            .fromCouple(comment.getCouple())
            .content(content)
            .build();
        Notification notificationRight = Notification.builder()
            .type(NotificationType.COMMENT)
            .to(replyTo.getCouple().getUserRight())
            .fromCouple(comment.getCouple())
            .content(content)
            .build();

        notificationService.send(notificationLeft);
        notificationService.send(notificationRight);
    }
}
