package se.edugrade.java25.restaurant.dto;

import se.edugrade.java25.restaurant.model.Restaurant;

public record RestaurantResource(
        Long id,
        String name,
        String cuisine,
        String city,
        int seats) {

    public static RestaurantResource from(Restaurant restaurant) {
        return new RestaurantResource(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getCuisine(),
                restaurant.getCity(),
                restaurant.getSeats()
        );
    }
}
