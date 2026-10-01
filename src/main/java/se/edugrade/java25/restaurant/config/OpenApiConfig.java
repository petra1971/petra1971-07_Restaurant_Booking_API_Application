package se.edugrade.java25.restaurant.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Document metadata - the part springdoc cannot read off the code.
 *
 * <p>Without this bean the generated document is titled "OpenAPI definition"
 * and versioned "v0". Both are true and neither is useful to the person who
 * has to integrate against it.</p>
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI restaurantOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Restaurant API")
                .version("2.0.0")
                .description("""
                        Restaurant booking management over HTTP.
                        """)
                .contact(new Contact().name("Petra Andréasson").email("petra.andreasson.java25@edu.edugrade.se"))
                .license(new License().name("Course material")));
    }
}
