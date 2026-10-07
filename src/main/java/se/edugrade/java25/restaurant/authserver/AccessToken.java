package se.edugrade.java25.restaurant.authserver;

/**
 * An issued access token.
 *
 * <p>Deliberately opaque: this chapter is about the flow that produces the
 * token, not its internal structure.
 *
 * @param token    the bearer token value the resource server checks
 * @param clientId the client this token was issued to
 * @param scope    what the token is allowed to do
 */
public record AccessToken(String token, String clientId, String scope) {
}
