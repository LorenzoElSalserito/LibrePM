package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.ConflictException;
import com.lorenzodm.librepm.api.exception.ForbiddenException;
import com.lorenzodm.librepm.api.exception.ResourceNotFoundException;
import com.lorenzodm.librepm.core.entity.RemoteIdentity;
import com.lorenzodm.librepm.core.entity.User;
import com.lorenzodm.librepm.repository.RemoteIdentityRepository;
import com.lorenzodm.librepm.repository.UserRepository;
import com.lorenzodm.librepm.service.RemoteIdentityService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class RemoteIdentityServiceImpl implements RemoteIdentityService {

    private final RemoteIdentityRepository identityRepository;
    private final UserRepository userRepository;

    public RemoteIdentityServiceImpl(RemoteIdentityRepository identityRepository,
                                     UserRepository userRepository) {
        this.identityRepository = identityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public RemoteIdentity bind(String userId, RemoteIdentity.Provider provider,
                               String providerUserId, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        if (identityRepository.existsByProviderAndProviderUserId(provider, providerUserId)) {
            throw new ConflictException("This provider identity is already bound to an account");
        }

        RemoteIdentity identity = new RemoteIdentity();
        identity.setUser(user);
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        identity.setEmail(email);
        identity.setBoundAt(Instant.now());

        return identityRepository.save(identity);
    }

    @Override
    public void unbind(String identityId, String userId) {
        RemoteIdentity identity = identityRepository.findById(identityId)
                .orElseThrow(() -> new ResourceNotFoundException("Remote identity not found: " + identityId));

        if (!identity.getUser().getId().equals(userId)) {
            throw new ForbiddenException("Cannot unbind identity belonging to another user");
        }

        identity.setRevokedAt(Instant.now());
        identityRepository.delete(identity); // soft delete via @SQLDelete
    }

    @Override
    public List<RemoteIdentity> listByUser(String userId) {
        return identityRepository.findByUserId(userId);
    }
}
