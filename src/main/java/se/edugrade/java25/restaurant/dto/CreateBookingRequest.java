package se.edugrade.java25.restaurant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.model.Restaurant;
import java.time.LocalDateTime;

public record CreateBookingRequest(
        @NotNull Long restaurantId,
        @NotBlank String guestName,
        @NotBlank @Email String guestEmail,
        @NotNull LocalDateTime arrivalTime,
        @NotNull int partySize) {

        public static Booking toBooking(CreateBookingRequest request, Restaurant restaurant) {
                return new Booking(
                        request.guestName,
                        request.guestEmail,
                        request.arrivalTime,
                        request.partySize,
                        restaurant
                );
        }
}
