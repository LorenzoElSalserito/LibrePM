package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateRoleRequest;
import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.PermissionPolicy;
import com.lorenzodm.librepm.core.entity.Role;
import com.lorenzodm.librepm.repository.PermissionPolicyRepository;
import com.lorenzodm.librepm.repository.RoleRepository;
import com.lorenzodm.librepm.service.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private static final Logger log = LoggerFactory.getLogger(RoleServiceImpl.class);
    private final RoleRepository roleRepository;
    private final PermissionPolicyRepository permissionPolicyRepository;

    public RoleServiceImpl(RoleRepository roleRepository, PermissionPolicyRepository permissionPolicyRepository) {
        this.roleRepository = roleRepository;
        this.permissionPolicyRepository = permissionPolicyRepository;
    }

    @Override
    public Role create(CreateRoleRequest request) {
        log.debug("Creazione ruolo: {}", request.name());
        if (roleRepository.existsByNameIgnoreCase(request.name())) {
            throw new ConflictException("Ruolo già esistente: " + request.name());
        }
        Role role = new Role();
        role.setName(request.name().trim());
        role.setDescription(request.description());
        return roleRepository.save(role);
    }

    @Override
    @Transactional(readOnly = true)
    public Role getById(String id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ruolo non trovato: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Role> listAll() {
        return roleRepository.findAllActive();
    }

    @Override
    public void delete(String id) {
        log.debug("Eliminazione ruolo: {}", id);
        Role role = getById(id);
        roleRepository.delete(role);
    }

    @Override
    public void addPermission(String roleId, String permission) {
        log.debug("Aggiunta permesso '{}' al ruolo {}", permission, roleId);
        Role role = getById(roleId);
        if (permissionPolicyRepository.existsByRoleIdAndPermission(roleId, permission)) {
            throw new ConflictException("Permesso già assegnato: " + permission);
        }
        PermissionPolicy policy = new PermissionPolicy();
        policy.setRole(role);
        policy.setPermission(permission);
        permissionPolicyRepository.save(policy);
    }

    @Override
    public void removePermission(String roleId, String permission) {
        log.debug("Rimozione permesso '{}' dal ruolo {}", permission, roleId);
        PermissionPolicy policy = permissionPolicyRepository.findByRoleIdAndPermission(roleId, permission)
                .orElseThrow(() -> new ResourceNotFoundException("Permesso non trovato: " + permission));
        permissionPolicyRepository.delete(policy);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getPermissions(String roleId) {
        return permissionPolicyRepository.findPermissionsByRoleId(roleId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(String roleId, String permission) {
        return permissionPolicyRepository.existsByRoleIdAndPermission(roleId, permission);
    }
}
