package se.edugrade.java25.restaurant.authserver;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.edugrade.java25.restaurant.authserver.TokenResponses.AccessTokenResponse;
import se.edugrade.java25.restaurant.authserver.TokenResponses.ErrorResponse;

/**
 * TaskFlow's own identity provider, playing the authorization server role.
 *
 * <p>Two endpoints, one per hop this chapter owns:
 * <ul>
 *   <li>{@code GET /authorize} - validates the request and issues a code.</li>
 *   <li>{@code POST /token} - exchanges a code for an access token.</li>
 * </ul>
 *
 * <p>Every check here is deliberately visible instead of hidden behind a
 * library, because the point is to show students what an authorization
 * server actually verifies.
 *
 * <p><b>What this toy does not model</b> (say so out loud in the lecture):
 * there is no login page and no consent screen - {@code GET /authorize}
 * assumes the resource owner already exists and already approved, so it
 * issues a code on the first request. A real authorization server
 * authenticates the resource owner first and only issues a code after an
 * explicit "allow" click. There is also no token expiry, no refresh token,
 * and no revocation endpoint - Chapter 13 covers what a real token carries
 * and how its lifetime is managed.
 */
@RestController
public class AuthorizationServerController {

    private final ClientRegistry clients;
    private final AuthorizationCodeStore codes;
    private final TokenStore tokens;

    public AuthorizationServerController(ClientRegistry clients, AuthorizationCodeStore codes, TokenStore tokens) {
        this.clients = clients;
        this.codes = codes;
        this.tokens = tokens;
    }

    @GetMapping("/authorize")
    public ResponseEntity<?> authorize(
            @RequestParam String response_type,
            @RequestParam String client_id,
            @RequestParam String redirect_uri,
            @RequestParam(defaultValue = "tasks:read") String scope,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String code_challenge,
            @RequestParam(required = false) String code_challenge_method) {

        ResponseEntity<ErrorResponse> validationError =
                validateAuthorizationRequest(response_type, client_id, redirect_uri, code_challenge, code_challenge_method);
        if (validationError != null) {
            return validationError;
        }

        AuthorizationCode issued = codes.issue(client_id, redirect_uri, scope, code_challenge, code_challenge_method);

        String location = redirect_uri + "?code=" + urlEncode(issued.code()) + appendState(state);
        return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(location)).build();
    }

    @PostMapping("/token")
    public ResponseEntity<?> token(
            @RequestParam String grant_type,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String redirect_uri,
            @RequestParam(required = false) String client_id,
            @RequestParam(required = false) String code_verifier) {

        if (!"authorization_code".equals(grant_type)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("unsupported_grant_type",
                    "grant_type '" + grant_type + "' is not supported. Only authorization_code is implemented here. "
                            + "The password grant (ROPC) and the implicit grant are deprecated and refused here."));
        }
        return exchangeAuthorizationCode(code, redirect_uri, client_id, code_verifier);
    }

    private ResponseEntity<?> exchangeAuthorizationCode(String code, String redirectUri, String clientId, String codeVerifier) {
        var redeemed = codes.redeem(code);
        if (redeemed.isEmpty()) {
            return ResponseEntity.badRequest().body(new ErrorResponse("invalid_grant",
                    "authorization code is unknown, expired, or has already been used"));
        }
        AuthorizationCode authorizationCode = redeemed.get();

        if (!authorizationCode.clientId().equals(clientId)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("invalid_grant", "client_id does not match the code"));
        }
        if (!authorizationCode.redirectUri().equals(redirectUri)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("invalid_grant", "redirect_uri does not match the code"));
        }

        ClientRegistration registration = clients.find(clientId);
        if (registration == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("invalid_client", "unknown client_id"));
        }
        if (registration.pkceRequired()
                && (codeVerifier == null || !Pkce.matches(codeVerifier, authorizationCode.codeChallenge()))) {
            return ResponseEntity.badRequest().body(new ErrorResponse("invalid_grant",
                    "code_verifier is missing or does not match the code_challenge from the authorization request"));
        }

        AccessToken issued = tokens.issue(clientId, authorizationCode.scope());
        return ResponseEntity.ok(AccessTokenResponse.of(issued));
    }

    private ResponseEntity<ErrorResponse> validateAuthorizationRequest(
            String responseType, String clientId, String redirectUri, String codeChallenge, String codeChallengeMethod) {

        ClientRegistration registration = clients.find(clientId);
        if (registration == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse("invalid_client", "unknown client_id"));
        }
        // Exact match only. A prefix or substring match here would let an
        // attacker register "https://taskflow.example.attacker.test/callback"
        // and still pass a naive check.
        if (!registration.isRedirectUriRegistered(redirectUri)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("invalid_request",
                    "redirect_uri is not registered for this client - refusing to redirect anywhere"));
        }
        if (!"code".equals(responseType)) {
            return ResponseEntity.badRequest().body(new ErrorResponse("unsupported_response_type",
                    "response_type '" + responseType + "' is not supported. "
                            + "The implicit grant (response_type=token) is deprecated and refused here."));
        }
        if (registration.pkceRequired() && (codeChallenge == null || !"S256".equals(codeChallengeMethod))) {
            return ResponseEntity.badRequest().body(new ErrorResponse("invalid_request",
                    "this client must send code_challenge with code_challenge_method=S256"));
        }
        return null;
    }

    private String appendState(String state) {
        return state == null ? "" : "&state=" + urlEncode(state);
    }

    private static String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
