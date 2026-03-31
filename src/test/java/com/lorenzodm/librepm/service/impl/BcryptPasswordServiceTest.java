package com.lorenzodm.librepm.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BcryptPasswordService}.
 * Validates bcrypt hashing, legacy SHA-256 verification, and migration detection.
 */
class BcryptPasswordServiceTest {

    private BcryptPasswordService service;

    @BeforeEach
    void setUp() {
        service = new BcryptPasswordService();
    }

    // --- Hashing ---

    @Test
    @DisplayName("hash() produces a bcrypt hash starting with $2a$")
    void hashProducesBcryptFormat() {
        String hash = service.hash("testPassword123");
        assertNotNull(hash);
        assertTrue(hash.startsWith("$2a$12$"), "Hash should be bcrypt with strength 12");
        assertEquals(60, hash.length(), "Bcrypt hash should be 60 characters");
    }

    @Test
    @DisplayName("hash() produces different hashes for same input (salted)")
    void hashIsSalted() {
        String hash1 = service.hash("samePassword");
        String hash2 = service.hash("samePassword");
        assertNotEquals(hash1, hash2, "Bcrypt hashes should differ due to random salt");
    }

    // --- Verification (bcrypt) ---

    @Test
    @DisplayName("verify() succeeds with correct password against bcrypt hash")
    void verifyCorrectBcryptPassword() {
        String hash = service.hash("mySecurePassword");
        assertTrue(service.verify("mySecurePassword", hash));
    }

    @Test
    @DisplayName("verify() fails with wrong password against bcrypt hash")
    void verifyWrongBcryptPassword() {
        String hash = service.hash("mySecurePassword");
        assertFalse(service.verify("wrongPassword", hash));
    }

    // --- Verification (legacy SHA-256) ---

    @Test
    @DisplayName("verify() succeeds with correct password against legacy SHA-256 hash")
    void verifyCorrectSha256Password() {
        String legacyHash = sha256Hex("legacyPassword");
        assertTrue(service.verify("legacyPassword", legacyHash));
    }

    @Test
    @DisplayName("verify() fails with wrong password against legacy SHA-256 hash")
    void verifyWrongSha256Password() {
        String legacyHash = sha256Hex("legacyPassword");
        assertFalse(service.verify("differentPassword", legacyHash));
    }

    // --- Migration detection ---

    @Test
    @DisplayName("needsMigration() returns false for bcrypt hashes")
    void bcryptDoesNotNeedMigration() {
        String hash = service.hash("password");
        assertFalse(service.needsMigration(hash));
    }

    @Test
    @DisplayName("needsMigration() returns true for SHA-256 hashes")
    void sha256NeedsMigration() {
        String legacyHash = sha256Hex("password");
        assertTrue(service.needsMigration(legacyHash));
    }

    @Test
    @DisplayName("needsMigration() returns false for null")
    void nullDoesNotNeedMigration() {
        assertFalse(service.needsMigration(null));
    }

    // --- Edge cases ---

    @Test
    @DisplayName("verify() returns false for null password")
    void verifyNullPassword() {
        String hash = service.hash("password");
        assertFalse(service.verify(null, hash));
    }

    @Test
    @DisplayName("verify() returns false for null hash")
    void verifyNullHash() {
        assertFalse(service.verify("password", null));
    }

    @Test
    @DisplayName("verify() returns false for empty hash")
    void verifyEmptyHash() {
        assertFalse(service.verify("password", ""));
    }

    @Test
    @DisplayName("Default local password hashes and verifies correctly")
    void defaultLocalPasswordWorks() {
        String defaultPwd = "local-profile-no-auth";
        // Legacy SHA-256 hash of default password
        String legacyHash = sha256Hex(defaultPwd);
        assertTrue(service.verify(defaultPwd, legacyHash),
                "Default local password should verify against its legacy hash");
        assertTrue(service.needsMigration(legacyHash),
                "Legacy hash should be flagged for migration");
    }

    // --- Helper ---

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
