package se.edugrade.java25.restaurant.authserver;

/**
 * A one-time-use authorization code, issued once the resource owner's
 * consent is assumed (see {@link AuthorizationServerController} for what
 * this demo does not model).
 *
 * @param code                short-lived, single-use value handed to the
 *                            client via the redirect
 * @param clientId            the client this code was issued to
 * @param redirectUri         must match, exactly, the redirect_uri the
 *                            client used when it started the flow
 * @param scope               what was approved
 * @param codeChallenge       the PKCE challenge from the authorization
 *                            request; {@code null} when the client is not
 *                            required to use PKCE
 * @param codeChallengeMethod always "S256" when {@code codeChallenge} is
 *                            present
 */
public record AuthorizationCode(
        String code,
        String clientId,
        String redirectUri,
        String scope,
        String codeChallenge,
        String codeChallengeMethod) {
}
