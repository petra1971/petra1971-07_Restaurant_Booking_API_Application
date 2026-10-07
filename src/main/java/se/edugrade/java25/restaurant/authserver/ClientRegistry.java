package se.edugrade.java25.restaurant.authserver;

import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

/**
 * TaskFlow's pre-registered OAuth2 clients.
 *
 * <p>Two registrations, each illustrating one client type from the chapter:
 * <ul>
 *   <li>{@code taskflow-app} - a public client (a phone app or single-page
 *       app). It cannot keep a secret, so PKCE is required.</li>
 *   <li>{@code taskflow-batch} - a confidential client with a secret. It
 *       registers no redirect URI at all, because it never sends a user
 *       through a browser; it exists only to show the contrast with a
 *       public client, not to exercise a working grant.</li>
 * </ul>
 */
@Component
public class ClientRegistry {

    public static final String RESTAURANT_APP_CLIENT_ID = "restaurant-app";
    public static final String RESTAURANT_APP_REDIRECT_URI = "https://restaurant.example/callback";
    public static final String RESTAURANT_BATCH_CLIENT_ID = "restaurant-batch";

    private final Map<String, ClientRegistration> clients = Map.of(
            RESTAURANT_APP_CLIENT_ID, new ClientRegistration(
                    RESTAURANT_APP_CLIENT_ID, null, Set.of(RESTAURANT_APP_REDIRECT_URI), true),
            RESTAURANT_BATCH_CLIENT_ID, new ClientRegistration(
                    RESTAURANT_BATCH_CLIENT_ID, "batch-job-secret-do-not-hardcode-in-real-life", Set.of(), false));

    public ClientRegistration find(String clientId) {
        return clients.get(clientId);
    }
}
