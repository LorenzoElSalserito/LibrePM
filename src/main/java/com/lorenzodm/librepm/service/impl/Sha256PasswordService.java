package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.service.PasswordService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Legacy SHA-256 password service without salt.
 * <p>
 * <b>DEPRECATED</b>: This service is retained only for backward compatibility
 * during the migration period. All new password hashing MUST use
 * {@link BcryptPasswordService}, which is marked as {@code @Primary}.
 * </p>
 * <p>
 * Existing SHA-256 hashes are transparently verified and migrated to bcrypt
 * at login time by {@link BcryptPasswordService#verify}.
 * </p>
 *
 * @author Lorenzo DM
 * @since 0.3.0
 * @deprecated Use {@link BcryptPasswordService} instead. Will be removed in v1.1.0.
 */
@Deprecated(since = "0.10.0", forRemoval = true)
public class Sha256PasswordService implements PasswordService {

    @Override
    public String hash(String rawPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 hashing failed", e);
        }
    }

    @Override
    public boolean verify(String rawPassword, String storedHash) {
        return hash(rawPassword).equals(storedHash);
    }

    @Override
    public boolean needsMigration(String storedHash) {
        // SHA-256 hashes always need migration to bcrypt
        return true;
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
