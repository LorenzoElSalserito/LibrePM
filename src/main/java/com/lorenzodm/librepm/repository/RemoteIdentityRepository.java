package com.lorenzodm.librepm.repository;

import com.lorenzodm.librepm.core.entity.RemoteIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RemoteIdentityRepository extends JpaRepository<RemoteIdentity, String> {

    List<RemoteIdentity> findByUserId(String userId);

    Optional<RemoteIdentity> findByProviderAndProviderUserId(
            RemoteIdentity.Provider provider, String providerUserId);

    boolean existsByProviderAndProviderUserId(
            RemoteIdentity.Provider provider, String providerUserId);
}
