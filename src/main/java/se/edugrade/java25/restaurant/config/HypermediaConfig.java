package se.edugrade.java25.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.hateoas.UriTemplate;
import org.springframework.hateoas.mediatype.hal.CurieProvider;
import org.springframework.hateoas.mediatype.hal.DefaultCurieProvider;

/**
 * Hänger ihop med HATEOS.
 * CURIE förklarar vad relationen som HATEOAS-länken representerar betyder.
 *
 * Namespaces our own link relations.
 *
 * <p>A bare {@code "confirm"} is a global claim on a common English word.
 * Registered IANA names ({@code self}, {@code collection}, {@code next}) stay
 * bare; ours come out as {@code taskflow:complete}, and the {@code curies}
 * entry in every response points at documentation a stranger can paste into a
 * browser.</p>
 *
 * <p>The {@code rel} is the contract, not the {@code href}. Change an href and
 * clients keep working, because they read the new one out of the response.
 * Change a rel and every client looking for the old name is broken.</p>
 */

@Configuration
public class HypermediaConfig {

    @Bean
    CurieProvider curieProvider() {

        // DefaultCurieProvider definierar en CURIE (Compact URI), alltså ett förkortat namn för en relations-URI.
        // bookings – prefixet som används framför relationsnamn i HATEOAS-länkarna t ex bookings:confirm
        //
        return new DefaultCurieProvider("bookings",
                UriTemplate.of(
                        // Länken fungerar inte så länge jag inte har skapat sidor med beskrivningar av länkarna
                        "https://07javawebservicesassignment1restaurantapi-production.up.railway.app/docs/rels/{rel}"));
    }
}
