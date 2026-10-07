package se.edugrade.java25.restaurant.authserver;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Where issued authorization codes live until they are redeemed - once.
 *
 * <p>{@link #redeem} uses {@link ConcurrentHashMap#remove}, which atomically
 * returns the value and deletes it in one step. The second call for the
 * same code finds nothing: that IS the single-use guarantee, not a
 * separate flag to remember to check.
 */
@Component
public class AuthorizationCodeStore {

    private final ConcurrentHashMap<String, AuthorizationCode> codes = new ConcurrentHashMap<>();

    public AuthorizationCode issue(String clientId, String redirectUri, String scope,
                                    String codeChallenge, String codeChallengeMethod) {
        String code = "AC-" + UUID.randomUUID();
        AuthorizationCode issued = new AuthorizationCode(code, clientId, redirectUri, scope, codeChallenge, codeChallengeMethod);
        codes.put(code, issued);
        return issued;
    }

    /** Redeems the code if it exists. A second call for the same code returns empty. */
    public Optional<AuthorizationCode> redeem(String code) {
        return Optional.ofNullable(codes.remove(code));
    }
}
