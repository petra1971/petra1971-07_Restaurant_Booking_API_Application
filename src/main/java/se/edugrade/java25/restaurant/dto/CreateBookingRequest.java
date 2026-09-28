package se.edugrade.java25.restaurant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.model.Restaurant;
import java.time.LocalDateTime;

public record CreateBookingRequest(
        @NotNull Long restaurantId,
        @NotBlank String bookersName,
        @NotBlank @Email String email,
        @NotBlank LocalDateTime arrivalTime,
        @NotNull Integer seats
        ) {

        public static Booking toBooking(CreateBookingRequest request, Restaurant restaurant) {
                return new Booking(
                        request.bookersName,
                        request.email,
                        request.arrivalTime,
                        request.seats,
                        restaurant
                );
            }
        }
