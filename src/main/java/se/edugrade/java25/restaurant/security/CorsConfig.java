package se.edugrade.java25.restaurant.security;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * The CORS form Spring Security's {@code CorsFilter} actually reads.
 * CorsConfig handlar om vilka andra webbplatser (origins) som får lov att anropa APIt från webbläsaren (browsern),
 * samt vilka metoder och headers som är tillåtna
 */
@Configuration
public class CorsConfig {

    static final String FRONTEND_ORIGIN = "http://localhost:5173";

    @Bean
    public org.springframework.web.cors.CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowedOrigins(List.of(FRONTEND_ORIGIN));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(List.of("Content-Type", "Authorization", "API-Version"));
        config.setExposedHeaders(List.of("X-Total-Count"));

        // Eftersom allowed Origins är specificerad är det okej med AllowCredentials(true)
        config.setAllowCredentials(true);
        config.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
