package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, String> {
    
    List<Notification> findByRecipientIdOrderByCreatedAtDesc(String recipientId);
    
    List<Notification> findByRecipientIdAndReadFalseOrderByCreatedAtDesc(String recipientId);
    
    long countByRecipientIdAndReadFalse(String recipientId);
}
