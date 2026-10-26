package se.edugrade.java25.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.edugrade.java25.restaurant.dto.*;
import se.edugrade.java25.restaurant.exception.RestaurantNotFoundException;
import se.edugrade.java25.restaurant.model.Restaurant;
import se.edugrade.java25.restaurant.repository.RestaurantRepository;

// Business logic lives here, not in the controller. The controller only
// maps DTOs and delegates.
@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public RestaurantResponse create(RestaurantRequest request) {
        Restaurant restaurant = RestaurantRequest.toRestaurant(request);
        Restaurant saved = restaurantRepository.save(restaurant);
        return RestaurantResponse.from(saved);
    }

    public Page<Restaurant> findAll(Pageable pageable) {
            return restaurantRepository.findAll(pageable);
    }

    public Restaurant findById(Long id) {
        return getRestaurantByIdOrThrow(id);
    }

    private Restaurant getRestaurantByIdOrThrow(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
    }
}
