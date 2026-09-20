package se.edugrade.java25.restaurant.dto;

import jakarta.validation.constraints.NotBlank;
import se.edugrade.java25.restaurant.entity.Booking;
import se.edugrade.java25.restaurant.entity.BookingStatus;
import se.edugrade.java25.restaurant.entity.Restaurant;
import java.time.LocalDateTime;

public record BookingRequest(
        @NotBlank String bookersName,
        @NotBlank String email,
        @NotBlank LocalDateTime arrivalTime,
        @NotBlank int bookingSize,
        @NotBlank BookingStatus status) {

        public static Booking toBooking(BookingRequest request, Restaurant restaurant) {
                return new Booking(
                        request.bookersName,
                        request.email,
                        request.arrivalTime,
                        request.bookingSize,
                        restaurant,
                        request.status
                );
            }
        }
