package org.mytool;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Compares two files by their SHA-256 digest.
 *
 * @author li1345825138
 * @date 2026/10/10
 */
final class HashComparer {

    record Comparison(String file1Hash, String file2Hash, boolean equal) {
    }

    Comparison compare(Path file1, Path file2) throws IOException {
        String hash1 = sha256(file1);
        String hash2 = sha256(file2);
        return new Comparison(hash1, hash2, hash1.equals(hash2));
    }

    private static String sha256(Path file) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is required but unavailable", e);
        }
        // Streamed rather than read whole, so comparing large files holds
        // constant memory instead of two full byte arrays.
        byte[] buffer = new byte[8192];
        try (InputStream input = Files.newInputStream(file)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }
        return HexFormat.of().withUpperCase().formatHex(digest.digest());
    }
}
