package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, String> {

    List<NotificationPreference> findByUserId(String userId);

    Optional<NotificationPreference> findByUserIdAndEventTypeAndChannel(
            String userId, String eventType, NotificationPreference.Channel channel);
}
