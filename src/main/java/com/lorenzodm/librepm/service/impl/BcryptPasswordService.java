package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.service.PasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Pattern;

/**
 * Secure password service using bcrypt with strength 12.
 * <p>
 * Supports transparent migration from legacy SHA-256 hashes:
 * when {@link #verify} encounters a SHA-256 hash, it verifies using the legacy
 * algorithm and signals that migration is needed via {@link #needsMigration}.
 * </p>
 *
 * @author Lorenzo DM
 * @since 0.10.0
 */
@Service
@Primary
public class BcryptPasswordService implements PasswordService {

    private static final Logger log = LoggerFactory.getLogger(BcryptPasswordService.class);

    /** Bcrypt encoder with strength 12 (2^12 = 4096 rounds). */
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    /** Pattern matching bcrypt hashes: $2a$, $2b$, or $2y$ prefix. */
    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

    /** SHA-256 hex digest is exactly 64 lowercase hex characters. */
    private static final Pattern SHA256_HEX_PATTERN = Pattern.compile("^[0-9a-f]{64}$");

    @Override
    public String hash(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    @Override
    public boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) {
            return false;
        }

        if (isBcryptHash(storedHash)) {
            return encoder.matches(rawPassword, storedHash);
        }

        if (isSha256Hash(storedHash)) {
            // Legacy verification: hash the input with SHA-256 and compare
            String legacyHash = sha256Hex(rawPassword);
            return legacyHash.equals(storedHash);
        }

        // Unknown format — reject
        log.warn("Password hash in unknown format (length={}), verification rejected", storedHash.length());
        return false;
    }

    @Override
    public boolean needsMigration(String storedHash) {
        if (storedHash == null) {
            return false;
        }
        // Any non-bcrypt hash needs migration
        return !isBcryptHash(storedHash);
    }

    /**
     * Checks if the hash is in bcrypt format.
     */
    private boolean isBcryptHash(String hash) {
        return BCRYPT_PATTERN.matcher(hash).matches();
    }

    /**
     * Checks if the hash looks like a legacy SHA-256 hex digest.
     */
    private boolean isSha256Hash(String hash) {
        return SHA256_HEX_PATTERN.matcher(hash).matches();
    }

    /**
     * Computes SHA-256 hex digest (for legacy verification only).
     */
    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
