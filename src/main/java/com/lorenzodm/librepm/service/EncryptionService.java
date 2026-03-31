package com.lorenzodm.librepm.service;

/**
 * Service for AES-256-GCM encryption/decryption of exported data.
 * Uses PBKDF2 for key derivation from user passphrase.
 */
public interface EncryptionService {

    /**
     * Encrypts data using AES-256-GCM with a passphrase-derived key.
     *
     * @param data       plaintext data
     * @param passphrase user-provided passphrase
     * @return encrypted result containing ciphertext, salt, and IV (all Base64-encoded)
     */
    EncryptedPayload encrypt(byte[] data, String passphrase);

    /**
     * Decrypts data encrypted with {@link #encrypt(byte[], String)}.
     *
     * @param payload    the encrypted payload (ciphertext, salt, IV)
     * @param passphrase user-provided passphrase
     * @return decrypted plaintext data
     */
    byte[] decrypt(EncryptedPayload payload, String passphrase);

    record EncryptedPayload(String ciphertext, String salt, String iv) {}
}
