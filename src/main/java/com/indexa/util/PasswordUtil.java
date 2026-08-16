package com.indexa.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Handles password hashing so plain-text passwords are never stored
 * in the database, per the project's security rules.
 *
 * Uses SHA-256 combined with a random per-user salt. The salt is
 * necessary because without it, two users with the same password
 * would produce the identical hash, and precomputed "rainbow table"
 * attacks become possible. Storing "salt:hash" together in one string
 * means we don't need an extra database column - hashPassword()
 * generates a fresh salt every time, and verifyPassword() re-reads
 * the same salt back out of the stored value to check a login attempt.
 */
public class PasswordUtil {

    private static final int SALT_LENGTH_BYTES = 16;

    /**
     * Hashes a plain-text password with a freshly generated random
     * salt. Returns a single string formatted as "saltBase64:hashBase64"
     * ready to store directly in the USERS.password_hash column.
     */
    public static String hashPassword(String plainPassword) {
        byte[] salt = generateSalt();
        byte[] hash = sha256(plainPassword, salt);

        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Checks a login attempt's plain-text password against a stored
     * "salt:hash" value from the database. Returns true only if they
     * match exactly.
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        String[] parts = storedHash.split(":", 2);
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

        byte[] actualHash = sha256(plainPassword, salt);
        return java.util.Arrays.equals(expectedHash, actualHash);
    }

    // ---------- Internal helpers ----------

    private static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static byte[] sha256(String plainPassword, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            return digest.digest(plainPassword.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is a standard algorithm guaranteed to exist on
            // every JVM, so this should never actually happen.
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
