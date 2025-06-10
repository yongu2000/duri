package com.duri.domain.notification.entity;

import com.duri.domain.couple.entity.Couple;
import com.duri.domain.notification.constant.NotificationType;
import com.duri.domain.user.entity.User;
import com.duri.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@SQLDelete(sql = "UPDATE notification SET deleted_at = CURRENT_TIMESTAMP WHERE notification_id = ?")
@SQLRestriction("deleted_at is null")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", updatable = false)
    private Long id;

    @Column(nullable = false)
    private String content;

    @ManyToOne
    @JoinColumn(name = "user_to_id", nullable = false)
    private User to;

    @ManyToOne
    @JoinColumn(name = "user_from_id")
    private User from;

    @ManyToOne
    @JoinColumn(name = "couple_from_id")
    private Couple fromCouple;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255)")
    private NotificationType type;

    @Builder.Default
    private boolean confirmed = false;
}
