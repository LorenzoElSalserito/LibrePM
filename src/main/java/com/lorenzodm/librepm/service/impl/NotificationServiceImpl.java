package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ForbiddenException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.Notification;
import com.lorenzodm.librepm.core.entity.NotificationPreference;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.NotificationPreferenceRepository;
import com.lorenzodm.librepm.repository.NotificationRepository;
import com.lorenzodm.librepm.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationPreferenceRepository preferenceRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                    NotificationPreferenceRepository preferenceRepository) {
        this.notificationRepository = notificationRepository;
        this.preferenceRepository = preferenceRepository;
    }

    @Override
    public void create(User recipient, User sender, Notification.NotificationType type, String message, String refType, String refId) {
        // Check user preferences before creating notification
        if (!isNotificationEnabled(recipient.getId(), type.name(), NotificationPreference.Channel.IN_APP)) {
            return; // User has disabled this notification type
        }

        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setSender(sender);
        notification.setType(type);
        notification.setMessage(message);
        notification.setReferenceType(refType);
        notification.setReferenceId(refId);
        notificationRepository.save(notification);
    }

    /**
     * Checks if a notification type is enabled for a user on a given channel.
     * If no preference is found, defaults to enabled.
     */
    private boolean isNotificationEnabled(String userId, String eventType, NotificationPreference.Channel channel) {
        Optional<NotificationPreference> pref = preferenceRepository
                .findByUserIdAndEventTypeAndChannel(userId, eventType, channel);
        return pref.map(NotificationPreference::isEnabled).orElse(true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> listUnread(String userId) {
        return notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> listAll(String userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUnread(String userId) {
        return notificationRepository.countByRecipientIdAndReadFalse(userId);
    }

    @Override
    public void markAsRead(String userId, String notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notifica non trovata"));

        if (!notification.getRecipient().getId().equals(userId)) {
            throw new ForbiddenException("Non puoi gestire le notifiche di altri utenti");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByRecipientIdAndReadFalseOrderByCreatedAtDesc(userId);
        unread.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(unread);
    }
}
