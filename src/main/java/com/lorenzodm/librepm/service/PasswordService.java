package com.lorenzodm.librepm.service;

/**
 * Service for secure password hashing and verification.
 * <p>
 * Implementations must use cryptographically secure algorithms with proper salting.
 * The {@link #verify} method handles transparent migration from legacy hash formats.
 * </p>
 *
 * @author Lorenzo DM
 * @since 0.3.0
 */
public interface PasswordService {

    /**
     * Hashes a raw password using a secure algorithm with automatic salting.
     *
     * @param rawPassword the plaintext password to hash
     * @return the securely hashed password string
     */
    String hash(String rawPassword);

    /**
     * Verifies a raw password against a stored hash.
     * Supports both current (bcrypt) and legacy (SHA-256) hash formats
     * to enable transparent migration.
     *
     * @param rawPassword the plaintext password to verify
     * @param storedHash  the stored hash to verify against
     * @return true if the password matches the stored hash
     */
    boolean verify(String rawPassword, String storedHash);

    /**
     * Checks whether a stored hash uses a legacy format that should be migrated.
     *
     * @param storedHash the stored hash to check
     * @return true if the hash should be re-hashed with the current algorithm
     */
    boolean needsMigration(String storedHash);
}
