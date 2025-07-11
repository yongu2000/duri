package com.duri.domain.post.entity;

import com.duri.domain.couple.entity.Couple;
import com.duri.domain.post.constant.PostStatus;
import com.duri.domain.post.constant.Scope;
import com.duri.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@SQLDelete(sql = "UPDATE post SET deleted_at = CURRENT_TIMESTAMP WHERE post_id = ?")
@SQLRestriction("deleted_at is null")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", updatable = false)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String placeName;
    private String placeUrl;
    private String category;
    private String phone;
    private String address;
    private String roadAddress;
    private Double x;
    private Double y;

    private LocalDate date;

    private Double rate;

    private Integer userLeftRate;
    private String userLeftComment;
    private Integer userRightRate;
    private String userRightComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "couple_id", nullable = false)
    private Couple couple;

    @Enumerated(EnumType.STRING)
    private Scope scope;

    @Enumerated(EnumType.STRING)
    private PostStatus status;

    @OneToOne(mappedBy = "post", fetch = FetchType.LAZY, orphanRemoval = true)
    private PostStat postStat;

    public void changeTitle(String title) {
        this.title = title;
    }

    public void changePlaceName(String placeName) {
        this.placeName = placeName;
    }

    public void changePlaceUrl(String placeUrl) {
        this.placeUrl = placeUrl;
    }

    public void changeCategory(String category) {
        this.category = category;
    }

    public void changePhone(String phone) {
        this.phone = phone;
    }

    public void changeAddress(String address) {
        this.address = address;
    }

    public void changeRoadAddress(String roadAddress) {
        this.roadAddress = roadAddress;
    }

    public void changeCoordinate(Double x, Double y) {
        this.x = x;
        this.y = y;
    }

    public void changeDate(LocalDate date) {
        this.date = date;
    }

    public void changeScope(Scope scope) {
        this.scope = scope;
    }

    public void changeStatus(PostStatus status) {
        this.status = status;
    }

    public void changeRate(Double rate) {
        this.rate = rate;
    }

    public void changeUserLeftRate(Integer userLeftRate) {
        this.userLeftRate = userLeftRate;
    }

    public void changeUserLeftComment(String userLeftComment) {
        this.userLeftComment = userLeftComment;
    }

    public void changeUserRightRate(Integer userRightRate) {
        this.userRightRate = userRightRate;
    }

    public void changeUserRightComment(String userRightComment) {
        this.userRightComment = userRightComment;
    }

}
