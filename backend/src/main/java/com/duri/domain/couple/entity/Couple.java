package com.duri.domain.couple.entity;

import com.duri.domain.user.entity.Gender;
import com.duri.domain.user.entity.Position;
import com.duri.domain.user.entity.User;
import com.duri.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@SQLDelete(sql = "UPDATE couple SET deleted_at = CURRENT_TIMESTAMP WHERE couple_id = ?")
@SQLRestriction("deleted_at is null")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Couple extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "couple_id", updatable = false)
    private Long id;
    private String name;
    private String code;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_left")
    private User userLeft;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_right")
    private User userRight;
    private String bio;

    public static Couple of(User userA, User userB) {
        Couple couple = new Couple();
        couple.code = UUID.randomUUID().toString();

        // 성별에 따라 순서 결정하도록 여기서 결정
        determineCoupleUserSequence(couple, userA, userB);

        userA.setCoupleCode(couple.code);
        userB.setCoupleCode(couple.code);
        couple.name = couple.getUserLeft().getName() + "&" + couple.getUserRight().getName();

        return couple;
    }

    private static void determineCoupleUserSequence(Couple couple, User userA, User userB) {
        Gender genderA = userA.getGender();
        Gender genderB = userB.getGender();

        // 성별이 다르면: male이 left, female이 right
        if (!genderA.equals(genderB)) {
            switch (genderA) {
                case Gender.MALE:
                    couple.userLeft = userA;
                    userA.setPosition(Position.LEFT);
                    couple.userRight = userB;
                    userB.setPosition(Position.RIGHT);
                    break;

                case FEMALE:
                    couple.userLeft = userB;
                    userB.setPosition(Position.LEFT);
                    couple.userRight = userA;
                    userA.setPosition(Position.RIGHT);
                    break;
            }
        } else {
            // 성별 같으면 순서대로 할당
            couple.userLeft = userA;
            userA.setPosition(Position.LEFT);
            couple.userRight = userB;
            userB.setPosition(Position.RIGHT);
        }
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeCode(String code) {
        this.code = code;
    }

    public void changeBio(String bio) {
        this.bio = bio;
    }

}
