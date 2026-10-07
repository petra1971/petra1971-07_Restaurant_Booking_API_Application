package se.edugrade.java25.restaurant.authserver;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import se.edugrade.java25.restaurant.security.SecurityConfig;

/**
 * TaskFlow playing resource server: the one filter that turns a bearer
 * token into an authenticated request.
 *
 * <p>No header at all, or a header that is not {@code "Bearer <token>"}, or
 * a token the {@link TokenStore} does not know about - all three leave the
 * request unauthenticated. It is {@link SecurityConfig}'s
 * {@code authorizeHttpRequests} rule for {@code /tasks/**} that then turns
 * "unauthenticated" into a 401, not this filter - this filter only ever
 * decides whether to populate the security context, never whether to
 * reject the request.
 */
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenStore tokens;

    public BearerTokenAuthenticationFilter(TokenStore tokens) {
        this.tokens = tokens;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            String presentedToken = header.substring(BEARER_PREFIX.length());
            tokens.validate(presentedToken).ifPresent(accessToken -> {
                var authentication = new UsernamePasswordAuthenticationToken(accessToken.clientId(), null, List.of());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            });
        }
        chain.doFilter(request, response);
    }
}
