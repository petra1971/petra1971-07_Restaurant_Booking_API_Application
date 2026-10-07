package se.edugrade.java25.restaurant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.accept.StandardApiVersionDeprecationHandler;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

/**
 * API versioning, native since Spring Framework 7.
 */
@Configuration
public class ApiVersioningConfig implements WebMvcConfigurer {

    /**
     * The header name is a convention, not a standard. Nothing in any RFC says
     * "API-Version" - which is exactly the argument against header versioning:
     * it is invisible in a browser, invisible in an access log, and every API
     * spells it differently.
     */
    public static final String VERSION_HEADER = "API-Version";

    @Override
    public void configureApiVersioning(ApiVersionConfigurer configurer) {
        configurer
                .useRequestHeader(VERSION_HEADER)

                // NOT the default. Out of the box a version is required, and a
                // request without the header gets 400. Optional + a default is the migration-safe
                // setting; "required" is a decision you make later, on purpose.
                .setVersionRequired(false)
                .setDefaultVersion("1")

                // Supported versions are detected from the declared mappings,
                // so API-Version: 99 answers 400 without us listing anything.
                .setDeprecationHandler(deprecationHandler());
    }

    /**
     * RFC 9745 ({@code Deprecation}) and RFC 8594 ({@code Sunset}) as
     * framework configuration rather than as headers you remember to set.
     *
     * <p>The order matters and is easy to get wrong: {@code Deprecation} is
     * when the announcement was made, {@code Sunset} is when the endpoint
     * stops answering. A Sunset that precedes its Deprecation announces a
     * removal that already happened.</p>
     */
    private StandardApiVersionDeprecationHandler deprecationHandler() {
        var handler = new StandardApiVersionDeprecationHandler();
        handler.configureVersion("1")

                // NOT optional, however much it looks like it. Without a
                // predicate the handler stamps Deprecation and Sunset on
                // *every* response: setDefaultVersion above resolves every
                // request without the header to version 1, including requests
                // to endpoints that were never versioned at all. A client
                // calling POST /restaurants would be told the endpoint retires in
                // December, which is not true and not something we can take
                // back once a partner has read it.
                //
                // Only /restaurants/{id} has two contracts, so only /restaurants/{id}
                // announces a retirement.
                .setRequestPredicate(request -> request.getRequestURI().matches("/restaurants/\\d+"))

                .setDeprecationDate(ZonedDateTime.of(2026, 3, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setSunsetDate(ZonedDateTime.of(2026, 12, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setDeprecationLink(URI.create("https://api.restaurantapi.example/docs/deprecation/v1"));
        return handler;
    }
}
