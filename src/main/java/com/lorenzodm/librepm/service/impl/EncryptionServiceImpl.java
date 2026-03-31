package com.lorenzodm.librepm.service.impl;

import com.lorenzodm.librepm.api.exception.BadRequestException;
import com.lorenzodm.librepm.service.EncryptionService;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * AES-256-GCM encryption with PBKDF2 key derivation.
 */
@Service
public class EncryptionServiceImpl implements EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_LENGTH = 256;
    private static final int IV_LENGTH = 12;  // 96 bits recommended for GCM
    private static final int SALT_LENGTH = 16;
    private static final int PBKDF2_ITERATIONS = 100_000;
    private static final int GCM_TAG_LENGTH = 128;

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public EncryptedPayload encrypt(byte[] data, String passphrase) {
        try {
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);

            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            SecretKey key = deriveKey(passphrase, salt);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] ciphertext = cipher.doFinal(data);

            return new EncryptedPayload(
                    Base64.getEncoder().encodeToString(ciphertext),
                    Base64.getEncoder().encodeToString(salt),
                    Base64.getEncoder().encodeToString(iv)
            );
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public byte[] decrypt(EncryptedPayload payload, String passphrase) {
        try {
            byte[] ciphertext = Base64.getDecoder().decode(payload.ciphertext());
            byte[] salt = Base64.getDecoder().decode(payload.salt());
            byte[] iv = Base64.getDecoder().decode(payload.iv());

            SecretKey key = deriveKey(passphrase, salt);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            return cipher.doFinal(ciphertext);
        } catch (javax.crypto.AEADBadTagException e) {
            throw new BadRequestException("Incorrect passphrase or corrupted data");
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }

    private SecretKey deriveKey(String passphrase, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(passphrase.toCharArray(), salt, PBKDF2_ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}
