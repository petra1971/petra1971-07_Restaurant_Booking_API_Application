package se.edugrade.java25.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
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
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
