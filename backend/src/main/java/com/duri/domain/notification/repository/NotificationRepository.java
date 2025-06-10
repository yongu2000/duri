package com.duri.domain.notification.repository;

import com.duri.domain.notification.entity.Notification;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NotificationRepository extends JpaRepository<Notification, Long>,
    NotificationSearchRepository {

    @Query("""
        SELECT n
        FROM Notification n
        LEFT JOIN FETCH n.to
        LEFT JOIN FETCH n.from
        WHERE n.to.id = :userId
        """)
    List<Notification> findByUserId(@Param("userId") Long userId);

    @Query("""
        SELECT COUNT(n)
        FROM Notification n
        WHERE n.to.id = :userId AND n.confirmed = FALSE
        """)
    long getUnconfirmedNotificationsCount(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Notification n SET n.confirmed = TRUE WHERE n.id IN :ids")
    void updateConfirmedByIds(@Param("ids") List<Long> ids);

    void deleteByToId(Long userId);
}
