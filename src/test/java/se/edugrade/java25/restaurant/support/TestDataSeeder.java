package se.edugrade.java25.restaurant.support;

import org.springframework.stereotype.Component;
import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.model.Restaurant;
import se.edugrade.java25.restaurant.repository.BookingRepository;
import se.edugrade.java25.restaurant.repository.RestaurantRepository;

import java.time.LocalDateTime;
import java.util.List;

public class TestDataSeeder {

    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;

    public TestDataSeeder(
            BookingRepository bookingRepository,
            RestaurantRepository restaurantRepository) {

        this.bookingRepository = bookingRepository;
        this.restaurantRepository = restaurantRepository;
    }

    public record TestIds(Long bookingId, Long restaurantId) {}

    public TestIds reset() {
        bookingRepository.deleteAll();
        restaurantRepository.deleteAll();

        Restaurant frantzen = new Restaurant("Frantzén", "Swedish", "Stockholm", 23);
        Restaurant oaxen = new Restaurant("Oaxen Krog", "Nordic", "Stockholm", 120);
        Restaurant bhoga = new Restaurant("Bhoga", "Swedish", "Gothenburg", 40);
        Restaurant mutantur = new Restaurant("Mutantur", "Swedish", "Malmö", 50);
        Restaurant vollmers = new Restaurant("Vollmers", "Swedish", "Malmö", 30);
        Restaurant lyran = new Restaurant("Lyran", "Nordic", "Malmö", 45);

        List<Restaurant> seededRestaurants = restaurantRepository.saveAll(List.of(
                frantzen, oaxen, bhoga, mutantur, vollmers, lyran));

        List<Booking> seededBookings = bookingRepository.saveAll(List.of(
                new Booking("Anna Andersson", "anna@example.com",
                        LocalDateTime.now().plusDays(1).withHour(18).withMinute(30),
                        2, frantzen),

                new Booking("Erik Johansson", "erik@example.com",
                        LocalDateTime.now().plusDays(2).withHour(19).withMinute(0),
                        4, vollmers),

                new Booking("Maria Svensson", "maria@example.com",
                        LocalDateTime.now().plusDays(3).withHour(20).withMinute(0),
                        3, bhoga)
        ));
        return new TestIds(
                seededBookings.get(0).getId(),
                seededRestaurants.get(0).getId()
        );
    }
}