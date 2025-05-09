package com.duri.domain.couple.entity;

import com.duri.domain.user.entity.User;
import com.duri.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;
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
    private String bio;

    @OneToMany(mappedBy = "couple", fetch = FetchType.LAZY)
    private List<User> users = new ArrayList<>();

    public static Couple of(User a, User b) {
        Couple couple = new Couple();
        couple.users.add(a);
        couple.users.add(b);
        // 성별에 따라 순서 결정하도록 여기서 결정
        a.setCouple(couple);
        b.setCouple(couple);
        couple.name = a.getName() + "&" + b.getName();
        return couple;
    }

}
