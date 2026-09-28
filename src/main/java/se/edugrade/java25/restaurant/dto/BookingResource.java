package se.edugrade.java25.restaurant.dto;

import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.model.BookingStatus;

import java.time.LocalDateTime;

public record BookingResource(
        Long id,
        String bookersName,
        String email,
        LocalDateTime arrivalTime,
        int bookingSize,
        BookingStatus status,
        Long restaurantId
) {
    public static BookingResource from(Booking booking) {
        return new BookingResource(
                booking.getId(),
                booking.getGuestName(),
                booking.getGuestEmail(),
                booking.getArrivalTime(),
                booking.getPartySize(),
                booking.getStatus(),
                booking.getRestaurant().getId()
        );
    }
}
