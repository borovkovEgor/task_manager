package com.example.messagebrokers.repository;

import com.example.messagebrokers.models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("select s from Notification s where s.userId=:userId and s.isRead=false")
    List<Notification> findIsNotReadByUserId(@Param("userId") Long userId);

    @Query("select s from Notification s where s.id=:id and s.userId=:userId")
    Optional<Notification> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

}