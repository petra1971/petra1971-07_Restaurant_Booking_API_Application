package se.edugrade.java25.restaurant.authserver;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * PKCE (RFC 7636): the client proves at token-exchange time that it is the
 * same party that started the flow, without ever putting a shared secret in
 * a public app.
 *
 * <p>The client generates a random {@code code_verifier} and keeps it to
 * itself. It sends only a hash of it - the {@code code_challenge} - in the
 * authorization request. Later, at the token endpoint, it reveals the
 * verifier. The authorization server hashes it again and checks the two
 * match. Anyone who only ever saw the challenge (an attacker who
 * intercepted the authorization code) cannot produce the verifier - hashes
 * do not run backwards.
 */
public final class Pkce {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder URL_ENCODER = Base64.getUrlEncoder().withoutPadding();

    private Pkce() {
    }

    /**
     * A random {@code code_verifier}: 32 random bytes, base64url-encoded
     * without padding. That yields 43 characters, inside the 43-128 range
     * RFC 7636 requires, drawn entirely from its unreserved character set
     * ({@code A-Z a-z 0-9 - . _ ~}).
     */
    public static String generateVerifier() {
        byte[] randomBytes = new byte[32];
        RANDOM.nextBytes(randomBytes);
        return URL_ENCODER.encodeToString(randomBytes);
    }

    /**
     * {@code code_challenge} = BASE64URL(SHA256(code_verifier)), no padding.
     * This is the "S256" method - the only one this authorization server
     * accepts. The plain method (challenge == verifier) exists in the RFC
     * but defeats the entire point, so it is not implemented here.
     */
    public static String challengeFor(String verifier) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha256.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return URL_ENCODER.encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is guaranteed present on every JDK; this cannot happen.
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    /** Does this verifier hash to the challenge that was stored earlier? */
    public static boolean matches(String verifier, String expectedChallenge) {
        return challengeFor(verifier).equals(expectedChallenge);
    }
}
