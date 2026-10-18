package se.edugrade.java25.restaurant.model;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import se.edugrade.java25.restaurant.repository.BookingRepository;
import se.edugrade.java25.restaurant.repository.RestaurantRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Six rows, seeded at startup. Enough to page over in step 3 without being
 * enough to scroll past during a demo.
 */
@Component
@Profile("!test") // Markerar att detta inte är data som anvädns i tester utan som används vid av applikationen lokatt
public class SampleData implements CommandLineRunner {

    private final BookingRepository bookings;
    private final RestaurantRepository restaurants;

    public SampleData(BookingRepository bookings, RestaurantRepository restaurants) {
        this.bookings = bookings;
        this.restaurants = restaurants;
    }

    @Override
    public void run(String... args) {
        if (bookings.count() > 0) {
            return;
        }

        Restaurant frantzen = new Restaurant("Frantzén", "Swedish", "Stockholm", 23);
        Restaurant oaxen = new Restaurant("Oaxen Krog", "Nordic", "Stockholm", 120);
        Restaurant bhoga = new Restaurant("Bhoga", "Swedish", "Gothenburg", 40);

        List<Restaurant> seededRestaurants = restaurants.saveAll(List.of(
                frantzen, oaxen, bhoga,
                new Restaurant("Mutantur", "Swedish", "Malmö", 50),
                new Restaurant("Vollmers", "Swedish", "Malmö", 30),
                new Restaurant("Lyran", "Nordic", "Malmö", 45)));

        List<Booking> seeded = bookings.saveAll(List.of(
                new Booking("Anna Andersson", "anna@example.com",
                        LocalDateTime.now().plusDays(1).withHour(18).withMinute(30),
                        2, frantzen),

                new Booking("Erik Johansson", "erik@example.com",
                        LocalDateTime.now().plusDays(2).withHour(19).withMinute(0),
                        4, frantzen),

                new Booking("Maria Svensson", "maria@example.com",
                        LocalDateTime.now().plusDays(3).withHour(20).withMinute(0),
                        3, bhoga)
        ));
    }
}
