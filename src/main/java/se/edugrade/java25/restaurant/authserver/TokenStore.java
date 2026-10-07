package se.edugrade.java25.restaurant.authserver;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Where issued access tokens live so the resource server's bearer-token
 * filter can check them (see
 * {@link BearerTokenAuthenticationFilter}).
 */
@Component
public class TokenStore {

    private final ConcurrentHashMap<String, AccessToken> tokens = new ConcurrentHashMap<>();

    public AccessToken issue(String clientId, String scope) {
        String token = "AT-" + UUID.randomUUID();
        AccessToken accessToken = new AccessToken(token, clientId, scope);
        tokens.put(token, accessToken);
        return accessToken;
    }

    public Optional<AccessToken> validate(String token) {
        return Optional.ofNullable(tokens.get(token));
    }
}
