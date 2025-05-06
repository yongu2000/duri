package com.duri.domain.couple.repository;

import com.duri.domain.couple.constant.CoupleConnectionStatus;
import com.duri.domain.couple.entity.CoupleConnection;
import com.duri.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoupleConnectionRepository extends JpaRepository<CoupleConnection, Long> {

    Optional<CoupleConnection> findByRequesterAndRespondentAndStatus(User requester,
        User Respondent, CoupleConnectionStatus status);

    Optional<CoupleConnection> findByRequester(User requester);

    Optional<CoupleConnection> findByRespondent(User respondent);


}
