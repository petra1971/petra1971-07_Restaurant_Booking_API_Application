package se.edugrade.java25.restaurant.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import se.edugrade.java25.restaurant.authserver.TokenStore;
import se.edugrade.java25.restaurant.config.BearerTokenAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final KeycloakJwtAuthenticationConverter jwtAuthenticationConverter;

    public SecurityConfig(KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter) {
        this.jwtAuthenticationConverter = keycloakJwtAuthenticationConverter; }

    /**
     * A stateless, bearer-token API. {@code SessionCreationPolicy.STATELESS}
     * means no session is ever created, so no session cookie exists for an
     * attacker's page to ride. Authentication comes from an Authorization
     * header that only this application's own JavaScript ever sets - a
     * plain cross-site form submission cannot add a custom header. That is
     * what makes disabling CSRF here correct.
     *
     * With built in Authorization server, which requires (in authserver package)
     *
     * ├── AccessToken
     * ├── AuthorizationCode
     * ├── AuthorizationCodeStore
     * ├── AuthorizationServerController
     * ├── ClientRegistration
     * ├── ClientRegistry
     * ├── Pkce
     * ├── TokenResponses
     * └── TokenStore
     */
//    @Bean
//    public BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter(TokenStore tokens) {
//        return new BearerTokenAuthenticationFilter(tokens);
//    }
//
//    @Bean
//    @Order(1)
//    public SecurityFilterChain statelessBearerChain(HttpSecurity http,BearerTokenAuthenticationFilter bearerTokenFilter) throws Exception {
//        http.securityMatcher("/bookings/**")
//                .csrf(AbstractHttpConfigurer::disable)
//                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .addFilterBefore(bearerTokenFilter, UsernamePasswordAuthenticationFilter.class)
//                .exceptionHandling(ex -> ex.authenticationEntryPoint(
//                        (request, response, authException) -> response.setStatus(401)))
//                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
//        return http.build();
//    }

    /**
     * With keycloak authorization server
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS ska hantera preflight-anrop innan authorization försöker
                // kontrollera användaren. En preflight är bara webbläsarens fråga
                // om CORS-regler och innehåller normalt ingen autentisering. Om en
                // request senare nekas ska CORS-headers fortfarande finnas med, så att
                // webbläsaren kan visa det riktiga felet, exempelvis 401 eller 403,
                // istället för att det ser ut som ett CORS-fel.

                // Nedan betyder: aktivera CORS och använd Spring Securitys normala sätt att konfigurera det"
                .cors(Customizer.withDefaults())

                // CSRF är avstängt eftersom ett stateless API som använder Bearer JWT
                // normalt inte behöver CSRF-skydd.
                .csrf(AbstractHttpConfigurer::disable)

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/health")
                        .permitAll()

//              // Om authorization-servern i projektet används (authserver) behövs denna regel
//                        .requestMatchers("/authorize", "/token")
//                        .permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"
                        ).permitAll()

                        .requestMatchers("/h2-console/**")
                        .permitAll()

                        .requestMatchers(HttpMethod.GET, "/bookings/**")
                        .hasRole("GUEST")

                        .requestMatchers(HttpMethod.POST, "/bookings")
                        .hasRole("GUEST")

                        .requestMatchers(HttpMethod.POST, "/bookings/{id}/confirm")
                        .hasRole("STAFF")

                        .requestMatchers(HttpMethod.POST, "/bookings/{id}/confirm")
                        .hasRole("STAFF")

                        .requestMatchers(HttpMethod.POST, "/bookings/{id}/seat")
                        .hasRole("STAFF")

                        .requestMatchers(HttpMethod.POST, "/bookings/{id}/cancel")
                        .hasRole("STAFF")

                        .requestMatchers(HttpMethod.POST, "/restaurants")
                        .hasRole("ADMIN")

                        .requestMatchers(HttpMethod.GET, "/restaurants/**")
                        .hasRole("ADMIN")

                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        jwtAuthenticationConverter
                                )
                        )
                );

        return http.build();
    }
}
