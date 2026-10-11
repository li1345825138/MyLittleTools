package org.mytool;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Random;

/**
 * Generates random passwords that a PasswordStore has not handed out before.
 *
 * @author li1345825138
 * @date 11/7/2024
 */
final class RandomPasswordGenerator {

    // password symbols
    private static final String SYMBOLS =
            "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz0123456789!#$@";

    /** Where the help text says a password length starts. */
    private static final int MIN_LENGTH = 8;

    /** Where the help text says a password length ends. */
    private static final int MAX_LENGTH = 1024;

    /**
     * How many times to regenerate when the store already holds the candidate.
     * With 62 symbols and a floor of 8 characters this is never reached in
     * practice; it exists so an exhausted space reports a failure instead of
     * spinning forever.
     */
    private static final int MAX_ATTEMPTS = 100;

    private final PasswordStore store;
    private final Random random = new SecureRandom();

    RandomPasswordGenerator(PasswordStore store) {
        this.store = store;
    }

    /**
     * Keep a requested length inside the range the help text promises, so a
     * zero or negative length cannot spin the generator forever.
     *
     * @param requested the length the user asked for
     * @return the requested length clamped to 8..1024
     */
    static int clampLength(int requested) {
        return Math.min(Math.max(requested, MIN_LENGTH), MAX_LENGTH);
    }

    /**
     * Generate a password and store it.
     *
     * @param requestedLength how long the password should be
     * @return a password the store had not seen before
     * @throws SQLException if the store cannot be written to
     */
    String generate(int requestedLength) throws SQLException {
        int length = clampLength(requestedLength);
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = randomString(length);
            if (store.storeIfAbsent(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
                "Could not generate an unused password in " + MAX_ATTEMPTS + " attempts");
    }

    private String randomString(int length) {
        StringBuilder candidate = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            candidate.append(SYMBOLS.charAt(random.nextInt(SYMBOLS.length())));
        }
        return candidate.toString();
    }
}
