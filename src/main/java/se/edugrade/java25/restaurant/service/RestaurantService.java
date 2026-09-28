package se.edugrade.java25.restaurant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import se.edugrade.java25.restaurant.model.Restaurant;
import se.edugrade.java25.restaurant.repository.RestaurantRepository;

import java.util.Optional;

// Business logic lives here, not in the controller. The controller only
// maps DTOs and delegates.
@Service
public class RestaurantService {

    private static final Logger log = LoggerFactory.getLogger(RestaurantService.class);

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public Restaurant create(String name, String cuisine, String city, int seats) {
        Restaurant restaurant = new Restaurant(name, cuisine, city, seats);
        return restaurantRepository.save(restaurant);
    }

    public Page<Restaurant> findAll(Pageable pageable) {
        return restaurantRepository.findAll(pageable);
    }

    //Tillfällig metod - GÖR OM
    public Optional<Restaurant> findById(Long id) {
        return restaurantRepository.findById(id);
    }
}
