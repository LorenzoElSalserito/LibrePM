package com.lorenzodm.librepm.service;

import com.lorenzodm.librepm.core.entity.RemoteIdentity;

import java.util.List;

/**
 * Service for managing remote identity bindings (OIDC/OAuth2).
 * Prepared for future SSO integration.
 */
public interface RemoteIdentityService {

    RemoteIdentity bind(String userId, RemoteIdentity.Provider provider,
                        String providerUserId, String email);

    void unbind(String identityId, String userId);

    List<RemoteIdentity> listByUser(String userId);
}
