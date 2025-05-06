package com.duri.domain.couple.entity;

import com.duri.domain.couple.constant.CoupleConnectionStatus;
import com.duri.domain.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CoupleConnection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "couple_connection_id")
    private Long id;
    private User requester;
    private User respondent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoupleConnectionStatus status;

    @Builder
    public CoupleConnection(User requester, User respondent, CoupleConnectionStatus status) {
        this.requester = requester;
        this.respondent = respondent;
        this.status = status;
    }
}
