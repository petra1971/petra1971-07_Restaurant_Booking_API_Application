package se.edugrade.java25.restaurant.authserver;

import java.util.Set;

/**
 * One OAuth2 client, as the authorization server knows it.
 *
 * <p>Registration is what makes the redirect URI check possible: the
 * authorization server only ever redirects to a URI it was told about in
 * advance, never to whatever a request happens to ask for.
 *
 * @param clientId     public identifier of the client
 * @param clientSecret shared secret for a confidential client; {@code null}
 *                     for a public client that only ever uses the
 *                     authorization code flow
 * @param redirectUris the exact URIs this client is allowed to be sent
 *                      back to - checked with string equality, never a
 *                      prefix or substring match
 * @param pkceRequired whether the token endpoint demands a matching
 *                      {@code code_verifier} for this client's codes
 */
public record ClientRegistration(
        String clientId,
        String clientSecret,
        Set<String> redirectUris,
        boolean pkceRequired) {

    public boolean isRedirectUriRegistered(String redirectUri) {
//        return redirectUris.stream().anyMatch(redirectUri::startsWith);
        return redirectUris.stream().anyMatch(redirectUri::equals);
    }
}
