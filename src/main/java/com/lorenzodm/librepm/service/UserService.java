package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateUserRequest;
import com.lorenzodm.librepm.api.dto.request.UpdateUserRequest;
import com.lorenzodm.librepm.core.entity.User;

import java.util.List;

public interface UserService {
    User create(CreateUserRequest req);
    User getById(String userId);
    List<User> list(boolean onlyActive);
    List<User> search(String query);
    User update(String userId, UpdateUserRequest req);
    User setActive(String userId, boolean active);
    void changePassword(String userId, String oldPassword, String newPassword);
    void removePassword(String userId, String currentPassword);
}
