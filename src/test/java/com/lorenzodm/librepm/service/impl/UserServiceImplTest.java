package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.BadRequestException;
import com.lorenzodm.librepm.api.exception.ForbiddenException;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    private UserRepository userRepository;
    private PasswordService passwordService;
    private UserServiceImpl service;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordService = mock(PasswordService.class);
        service = new UserServiceImpl(userRepository, passwordService);
    }

    @Test
    void resetPasswordSavesHashWhenUsernameMatches() {
        User user = user();
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordService.hash("Newpass1!")).thenReturn("hashed-new-password");

        service.resetPassword("u1", "  mario.rossi  ", "Newpass1!");

        verify(passwordService).hash("Newpass1!");
        verify(userRepository).save(user);
    }

    @Test
    void resetPasswordRejectsWrongUsername() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(user()));

        assertThrows(ForbiddenException.class,
                () -> service.resetPassword("u1", "luigi", "Newpass1!"));

        verify(passwordService, never()).hash(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void resetPasswordRejectsWeakPassword() {
        when(userRepository.findById("u1")).thenReturn(Optional.of(user()));

        assertThrows(BadRequestException.class,
                () -> service.resetPassword("u1", "mario.rossi", "weak"));

        verify(passwordService, never()).hash(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePasswordKeepsLegacyMinimumLengthCompatibility() {
        User user = user();
        when(userRepository.findById("u1")).thenReturn(Optional.of(user));
        when(passwordService.verify("old", "old-hash")).thenReturn(true);
        when(passwordService.hash("abcd")).thenReturn("hashed-short-password");

        service.changePassword("u1", "old", "abcd");

        verify(passwordService).hash("abcd");
        verify(userRepository).save(user);
    }

    private User user() {
        User user = new User();
        user.setId("u1");
        user.setUsername("mario.rossi");
        user.setPasswordHash("old-hash");
        return user;
    }
}
