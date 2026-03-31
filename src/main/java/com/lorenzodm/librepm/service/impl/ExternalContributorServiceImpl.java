package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.dto.request.CreateExternalContributorRequest;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.ExternalContributor;
import com.lorenzodm.librepm.core.entity.Role;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.ExternalContributorRepository;
import com.lorenzodm.librepm.repository.RoleRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.ExternalContributorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ExternalContributorServiceImpl implements ExternalContributorService {

    private final ExternalContributorRepository repository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public ExternalContributorServiceImpl(ExternalContributorRepository repository,
                                           UserRepository userRepository,
                                           RoleRepository roleRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public ExternalContributor create(String userId, CreateExternalContributorRequest req) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ExternalContributor ec = new ExternalContributor();
        ec.setDisplayName(req.displayName());
        ec.setEmail(req.email());
        ec.setOrganization(req.organization());
        ec.setCreatedBy(creator);

        if (req.roleId() != null) {
            Role role = roleRepository.findById(req.roleId())
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + req.roleId()));
            ec.setRole(role);
        }

        if (req.scope() != null) {
            ec.setScope(ExternalContributor.Scope.valueOf(req.scope()));
        }
        ec.setScopeEntityId(req.scopeEntityId());

        return repository.save(ec);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalContributor> listByScope(String scopeEntityId) {
        return repository.findByScopeEntityId(scopeEntityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExternalContributor> listAll() {
        return repository.findAllActive();
    }

    @Override
    public void revoke(String userId, String contributorId) {
        ExternalContributor ec = repository.findById(contributorId)
                .orElseThrow(() -> new ResourceNotFoundException("External contributor not found"));
        repository.delete(ec); // Soft delete via @SQLDelete
    }
}
