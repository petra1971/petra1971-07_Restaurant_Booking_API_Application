package se.edugrade.java25.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.edugrade.java25.restaurant.model.Restaurant;

public record RestaurantRequest(
        @NotBlank String name,
        @NotBlank String cuisine,
        @NotBlank String city,
        @NotNull int seats) {

        public static Restaurant toRestaurant(RestaurantRequest request) {
            return new Restaurant(
                    request.name,
                    request.cuisine,
                    request.city,
                    request.seats
            );
        }
}
