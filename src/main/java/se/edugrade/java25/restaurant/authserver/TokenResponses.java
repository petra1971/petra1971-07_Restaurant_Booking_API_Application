package se.edugrade.java25.restaurant.authserver;

/** Response bodies the {@code /token} endpoint can return. */
public final class TokenResponses {

    private TokenResponses() {
    }

    /** Successful grant: RFC 6749's minimal token response shape. */
    public record AccessTokenResponse(String access_token, String token_type, String scope) {
        public static AccessTokenResponse of(AccessToken token) {
            return new AccessTokenResponse(token.token(), "Bearer", token.scope());
        }
    }

    /** RFC 6749 error shape: a machine-readable error code plus a human hint. */
    public record ErrorResponse(String error, String error_description) {
    }
}
