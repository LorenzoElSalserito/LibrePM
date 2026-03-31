package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateUserRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateUserRequest;
import com.lorenzodm.librepm.api.exception.BadRequestException;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.ForbiddenException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.PasswordService;
import com.lorenzodm.librepm.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserServiceImpl(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    @Override
    public User create(CreateUserRequest req) {
        if (userRepository.existsByUsernameIgnoreCase(req.username())) {
            throw new ConflictException("Username già esistente: " + req.username());
        }
        if (req.email() != null && !req.email().isBlank() && userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ConflictException("Email già esistente: " + req.email());
        }

        User u = new User();
        u.setUsername(req.username());
        u.setEmail(req.email());
        u.setDisplayName(req.displayName());
        u.setPasswordHash(passwordService.hash(req.password()));
        u.setActive(true);

        return userRepository.save(u);
    }

    @Override
    public User getById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User non trovato: " + userId));
    }

    @Override
    public List<User> list(boolean onlyActive) {
        return onlyActive ? userRepository.findByActiveTrue() : userRepository.findAll();
    }

    @Override
    public List<User> search(String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return userRepository.searchRealUsers(query);
    }

    @Override
    public User update(String userId, UpdateUserRequest req) {
        User u = getById(userId);

        if (req.email() != null && !req.email().isBlank() && !req.email().equalsIgnoreCase(u.getEmail())) {
            if (userRepository.existsByEmailIgnoreCase(req.email())) {
                throw new ConflictException("Email già esistente: " + req.email());
            }
            u.setEmail(req.email());
        }

        if (req.displayName() != null) u.setDisplayName(req.displayName());
        if (req.avatarPath() != null) u.setAvatarPath(req.avatarPath());
        if (req.active() != null) u.setActive(req.active());

        return userRepository.save(u);
    }

    @Override
    public User setActive(String userId, boolean active) {
        User u = getById(userId);
        u.setActive(active);
        return userRepository.save(u);
    }

    @Override
    public void changePassword(String userId, String oldPassword, String newPassword) {
        User u = getById(userId);
        // If user has a password, verify old password
        if (u.getPasswordHash() != null && !u.getPasswordHash().isBlank()) {
            if (!passwordService.verify(oldPassword, u.getPasswordHash())) {
                throw new ForbiddenException("Current password is incorrect");
            }
        }
        if (newPassword == null || newPassword.length() < 4) {
            throw new BadRequestException("New password must be at least 4 characters");
        }
        u.setPasswordHash(passwordService.hash(newPassword));
        userRepository.save(u);
    }

    @Override
    public void removePassword(String userId, String currentPassword) {
        User u = getById(userId);
        if (u.getPasswordHash() != null && !u.getPasswordHash().isBlank()) {
            if (!passwordService.verify(currentPassword, u.getPasswordHash())) {
                throw new ForbiddenException("Current password is incorrect");
            }
        }
        u.setPasswordHash("");
        userRepository.save(u);
    }
}
