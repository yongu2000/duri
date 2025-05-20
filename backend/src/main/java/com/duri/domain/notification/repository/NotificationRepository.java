package com.duri.domain.notification.repository;

import com.duri.domain.notification.entity.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
        SELECT n
        FROM Notification n
        LEFT JOIN FETCH n.to
        LEFT JOIN FETCH n.from
        WHERE n.to.id = :userId
        """)
    List<Notification> findByUserId(@Param("userId") Long userId);
}
