package se.edugrade.java25.restaurant.exception;

public class BookingNotFoundException extends RuntimeException {

    public BookingNotFoundException(Long id) {
        super("Booking with id " + id + " not found");
    }
}
