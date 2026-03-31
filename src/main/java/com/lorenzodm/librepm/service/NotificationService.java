package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.Notification;
import com.lorenzodm.librepm.core.entity.User;

import java.util.List;

public interface NotificationService {
    void create(User recipient, User sender, Notification.NotificationType type, String message, String refType, String refId);
    List<Notification> listUnread(String userId);
    List<Notification> listAll(String userId);
    long countUnread(String userId);
    void markAsRead(String userId, String notificationId);
    void markAllAsRead(String userId);
}
