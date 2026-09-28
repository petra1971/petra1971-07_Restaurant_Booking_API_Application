package se.edugrade.java25.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import se.edugrade.java25.restaurant.dto.*;
import se.edugrade.java25.restaurant.exception.RestaurantNotFoundException;
import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.model.Restaurant;
import se.edugrade.java25.restaurant.repository.RestaurantRepository;

import java.util.Optional;

// Business logic lives here, not in the controller. The controller only
// maps DTOs and delegates.
@Service
public class RestaurantService {

    private final RestaurantRepository restaurants;

    private static final int MAX_PAGE_SIZE = 100;

    public RestaurantService(RestaurantRepository restaurants) {
        this.restaurants = restaurants;
    }

    @Transactional
    public RestaurantResource create(RestaurantRequest request) {
        Restaurant restaurant = RestaurantRequest.toRestaurant(request);
        Restaurant saved = restaurants.save(restaurant);
        return RestaurantResource.from(saved);
    }

    public OffsetRestaurantPage findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, clamp(size));
        Page<Restaurant> result = restaurants.findAll(pageable);

        return new OffsetRestaurantPage(
                result.getContent()
                        .stream()
                        .map(RestaurantResource::from)
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    public RestaurantResource findById(Long id) {
        Restaurant saved = getRestaurantByIdOrThrow(id);
        return RestaurantResource.from(saved);
    }

    //Ta requested men se till att värdet inte kan bli mindre än ett minsta värde eller större än ett största värde.
    private static int clamp(int requested) {
        return  Math.min(Math.max(requested, 1), MAX_PAGE_SIZE);
    }

    private Restaurant getRestaurantByIdOrThrow(Long id) {
        return restaurants.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
    }
}
