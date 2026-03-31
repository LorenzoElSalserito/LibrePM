package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.core.entity.Notification;
import com.lorenzodm.librepm.core.entity.ProjectMember;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.ProjectMemberRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.MentionService;
import com.lorenzodm.librepm.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Transactional
public class MentionServiceImpl implements MentionService {

    private static final Logger log = LoggerFactory.getLogger(MentionServiceImpl.class);
    private static final Pattern MENTION_PATTERN = Pattern.compile("@([a-zA-Z0-9._-]+)");

    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationService notificationService;

    public MentionServiceImpl(UserRepository userRepository,
                               ProjectMemberRepository projectMemberRepository,
                               NotificationService notificationService) {
        this.userRepository = userRepository;
        this.projectMemberRepository = projectMemberRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Set<String> extractMentions(String text) {
        if (text == null || text.isBlank()) return Set.of();
        Set<String> usernames = new LinkedHashSet<>();
        Matcher matcher = MENTION_PATTERN.matcher(text);
        while (matcher.find()) {
            usernames.add(matcher.group(1));
        }
        return usernames;
    }

    @Override
    public void processMentions(String text, String senderId, String projectId, String refType, String refId) {
        Set<String> usernames = extractMentions(text);
        if (usernames.isEmpty()) return;

        User sender = userRepository.findById(senderId).orElse(null);
        if (sender == null) return;

        // Resolve project members for context
        Set<String> memberUserIds = projectMemberRepository.findByProjectId(projectId).stream()
                .map(pm -> pm.getUser().getId())
                .collect(Collectors.toSet());

        for (String username : usernames) {
            userRepository.findByUsernameIgnoreCase(username).ifPresent(user -> {
                // Only notify if user is a project member and not the sender
                if (memberUserIds.contains(user.getId()) && !user.getId().equals(senderId)) {
                    String message = String.format("@%s mentioned you in %s", sender.getUsername(), refType.toLowerCase());
                    notificationService.create(user, sender, Notification.NotificationType.MENTION, message, refType, refId);
                    log.debug("Created mention notification for @{} from @{}", username, sender.getUsername());
                }
            });
        }
    }
}
