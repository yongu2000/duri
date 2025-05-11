package com.duri.domain.couple.repository;

import com.duri.domain.couple.entity.Couple;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CoupleRepository extends JpaRepository<Couple, Long> {

    @Query("""
        SELECT c FROM Couple c
        LEFT JOIN FETCH c.userLeft
        LEFT JOIN FETCH c.userRight
        WHERE c.code = :code
        """)
    Optional<Couple> findCoupleWithUsersByCode(String code);

    Optional<Couple> findByCode(String code);


}
