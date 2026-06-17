package br.com.cardapioonline.application.common;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.springframework.stereotype.Component;

@Component
public class PasswordHashService {

    private static final int SALT_SIZE = 16;
    private static final int HASH_SIZE = 32;
    private static final int ITERATIONS = 100_000;

    public String hash(String password) {
        byte[] salt = new byte[SALT_SIZE];
        new SecureRandom().nextBytes(salt);
        byte[] hash = pbkdf2(password.toCharArray(), salt, HASH_SIZE);
        return Base64.getEncoder().encodeToString(salt) + "." + Base64.getEncoder().encodeToString(hash);
    }

    public boolean verify(String password, String storedHash) {
        try {
            String[] parts = storedHash.split("\\.", 2);
            if (parts.length != 2) {
                return false;
            }

            byte[] salt = Base64.getDecoder().decode(parts[0].getBytes(StandardCharsets.UTF_8));
            byte[] expectedHash = Base64.getDecoder().decode(parts[1].getBytes(StandardCharsets.UTF_8));
            byte[] actualHash = pbkdf2(password.toCharArray(), salt, expectedHash.length);
            return java.security.MessageDigest.isEqual(actualHash, expectedHash);
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }

    private byte[] pbkdf2(char[] password, byte[] salt, int hashSize) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, hashSize * 8);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return factory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("Unable to hash password", ex);
        }
    }
}
