package com.lorenzodm.librepm.api.controller;

import com.lorenzodm.librepm.core.entity.ProjectMessage;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.ProjectMessageRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.PermissionChecker;
import com.lorenzodm.librepm.security.AuthProperties;
import com.lorenzodm.librepm.security.CurrentUserUtils;
import com.lorenzodm.librepm.api.exception.ForbiddenException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users/{userId}/projects/{projectId}/messages")
public class ProjectMessageController {
    private final ProjectMessageRepository messages;
    private final PermissionChecker permissions;
    private final AuthProperties auth;
    private final UserRepository users;
    public ProjectMessageController(ProjectMessageRepository messages, PermissionChecker permissions, AuthProperties auth, UserRepository users) {
        this.messages = messages;
        this.permissions = permissions;
        this.auth = auth;
        this.users = users;
    }
    public record SendMessage(@NotBlank @Size(max = 4000) String content) {}
    public record MessageResponse(String id, String projectId, String senderId, String senderName, String content, Instant createdAt) {}

    private List<MessageResponse> withSenderNames(List<ProjectMessage> page) {
        Set<String> senderIds = page.stream().map(ProjectMessage::getSenderId).collect(Collectors.toSet());
        Map<String, User> senders = users.findAllById(senderIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        return page.stream().map(message -> {
            User sender = senders.get(message.getSenderId());
            String name = sender == null ? null
                    : sender.getDisplayName() != null && !sender.getDisplayName().isBlank() ? sender.getDisplayName() : sender.getUsername();
            return new MessageResponse(message.getId(), message.getProjectId(), message.getSenderId(), name, message.getContent(), message.getCreatedAt());
        }).toList();
    }

    private void authorize(String userId, String projectId, String permission) {
        if (auth.isEnabled() && !CurrentUserUtils.getUserIdOrNull().filter(userId::equals).isPresent()) {
            throw new ForbiddenException("Authenticated identity does not match requested user");
        }
        permissions.requirePermission(userId, projectId, permission);
    }
    @GetMapping
    public List<MessageResponse> list(@PathVariable String userId, @PathVariable String projectId,
                                    @RequestParam(defaultValue = "0") int page) {
        authorize(userId, projectId, "MESSAGES_READ");
        if (page < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid page");
        return withSenderNames(messages.findByProjectIdOrderByCreatedAtDescIdDesc(projectId, PageRequest.of(page, 50)).getContent());
    }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MessageResponse send(@PathVariable String userId, @PathVariable String projectId,
                               @Valid @RequestBody SendMessage request) {
        authorize(userId, projectId, "MESSAGES_WRITE");
        return withSenderNames(List.of(messages.saveAndFlush(new ProjectMessage(projectId, userId, request.content().strip())))).get(0);
    }
}
