package se.edugrade.java25.restaurant.dto;

import se.edugrade.java25.restaurant.entity.Booking;
import se.edugrade.java25.restaurant.entity.BookingStatus;

import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        String bookersName,
        String email,
        LocalDateTime arrivalTime,
        int bookingSize,
        BookingStatus status,
        Long restaurantId
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getBookersName(),
                booking.getEmail(),
                booking.getArrivalTime(),
                booking.getBookingSize(),
                booking.getStatus(),
                booking.getRestaurant().getId()
        );
    }
}
