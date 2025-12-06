package com.utilitybill.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class for password hashing and verification.
 * Uses SHA-256 with salt for secure password storage.
 *
 * <p>Note: For production use, consider using BCrypt or Argon2.
 * This implementation is for educational purposes.</p>
 *
 * @author Utility Bill Management System
 * @version 1.0
 * @since 2024
 */
public final class PasswordUtil {

    /** Salt length in bytes */
    private static final int SALT_LENGTH = 16;

    /** Hash algorithm */
    private static final String ALGORITHM = "SHA-256";

    /** Separator between salt and hash in stored value */
    private static final String SEPARATOR = ":";

    /** Secure random number generator */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Private constructor to prevent instantiation.
     */
    private PasswordUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Hashes a password with a random salt.
     *
     * @param password the plain text password
     * @return the salted hash (format: base64(salt):base64(hash))
     * @throws IllegalStateException if the hashing algorithm is not available
     */
    public static String hashPassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        // Generate random salt
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);

        // Hash password with salt
        byte[] hash = hashWithSalt(password, salt);

        // Return salt:hash as base64 encoded strings
        String saltBase64 = Base64.getEncoder().encodeToString(salt);
        String hashBase64 = Base64.getEncoder().encodeToString(hash);

        return saltBase64 + SEPARATOR + hashBase64;
    }

    /**
     * Verifies a password against a stored hash.
     *
     * @param password   the plain text password to verify
     * @param storedHash the stored salted hash
     * @return true if the password matches
     */
    public static boolean verifyPassword(String password, String storedHash) {
        if (password == null || storedHash == null) {
            return false;
        }

        try {
            // Split stored hash into salt and hash
            String[] parts = storedHash.split(SEPARATOR);
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[1]);

            // Hash the provided password with the same salt
            byte[] actualHash = hashWithSalt(password, salt);

            // Compare hashes using constant-time comparison
            return constantTimeEquals(expectedHash, actualHash);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Hashes a password with a given salt.
     *
     * @param password the password to hash
     * @param salt     the salt to use
     * @return the hash bytes
     */
    private static byte[] hashWithSalt(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.update(salt);
            return digest.digest(password.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Hash algorithm not available: " + ALGORITHM, e);
        }
    }

    /**
     * Compares two byte arrays in constant time to prevent timing attacks.
     *
     * @param a first byte array
     * @param b second byte array
     * @return true if arrays are equal
     */
    private static boolean constantTimeEquals(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < a.length; i++) {
            result |= a[i] ^ b[i];
        }
        return result == 0;
    }

    /**
     * Generates a random password.
     *
     * @param length the desired password length
     * @return a random password
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) {
            length = 8;
        }

        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder password = new StringBuilder();

        // Ensure at least one of each required character type
        password.append(chars.charAt(SECURE_RANDOM.nextInt(26))); // Uppercase
        password.append(chars.charAt(26 + SECURE_RANDOM.nextInt(26))); // Lowercase
        password.append(chars.charAt(52 + SECURE_RANDOM.nextInt(10))); // Digit

        // Fill the rest randomly
        for (int i = 3; i < length; i++) {
            password.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }

        // Shuffle the password
        char[] passwordChars = password.toString().toCharArray();
        for (int i = passwordChars.length - 1; i > 0; i--) {
            int j = SECURE_RANDOM.nextInt(i + 1);
            char temp = passwordChars[i];
            passwordChars[i] = passwordChars[j];
            passwordChars[j] = temp;
        }

        return new String(passwordChars);
    }
}

