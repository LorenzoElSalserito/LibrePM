package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.api.dto.request.CreateRoleRequest;
import com.lorenzodm.librepm.core.entity.Role;

import java.util.List;

public interface RoleService {
    Role create(CreateRoleRequest request);
    Role getById(String id);
    List<Role> listAll();
    void delete(String id);
    void addPermission(String roleId, String permission);
    void removePermission(String roleId, String permission);
    List<String> getPermissions(String roleId);
    boolean hasPermission(String roleId, String permission);
}
