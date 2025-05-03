package com.duri.domain.user.entity;

import com.duri.global.entity.BaseEntity;
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
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@SQLDelete(sql = "UPDATE image SET deleted_at = CURRENT_TIMESTAMP WHERE image_id = ?")
@SQLRestriction("deleted_at is null")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", updatable = false)
    private Long id;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
    @Column(name = "profile_image_url")
    private String profileImageUrl;
    private String provider;

    @Builder
    public User(String email, String username, String password, String name, Role role,
        String profileImageUrl,
        String provider) {
        this.email = email;
        this.username = username;
        this.password = password;
        this.name = name;
        this.role = role;
        this.profileImageUrl = profileImageUrl;
        this.provider = provider;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }

}
