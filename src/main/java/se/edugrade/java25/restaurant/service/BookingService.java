package se.edugrade.java25.restaurant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.edugrade.java25.restaurant.dto.BookingResponse;
import se.edugrade.java25.restaurant.dto.BookingRequest;
import se.edugrade.java25.restaurant.entity.Booking;
import se.edugrade.java25.restaurant.entity.BookingStatus;
import se.edugrade.java25.restaurant.entity.Restaurant;
import se.edugrade.java25.restaurant.exception.BookingNotFoundException;
import se.edugrade.java25.restaurant.exception.RestaurantNotFoundException;
import se.edugrade.java25.restaurant.repository.BookingRepository;
import se.edugrade.java25.restaurant.repository.RestaurantRepository;

// Business logic lives here, not in the controller
@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;

    public BookingService(BookingRepository bookingRepository, RestaurantRepository restaurantRepository) {
        this.bookingRepository = bookingRepository;
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional
    public BookingResponse createBooking(Long restaurantId, BookingRequest request) {
        Restaurant restaurant = getRestaurantByIdOrThrow(restaurantId);
        Booking booking = BookingRequest.toBooking(request, restaurant);
        return BookingResponse.from(bookingRepository.save(booking));
    }

    public Page<BookingResponse> findAll(Pageable pageable) {
        return bookingRepository.findAll(pageable)
                .map(booking -> BookingResponse.from(booking));
    }

    public BookingResponse findById(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        return BookingResponse.from(booking);
    }

    // Lägg till fler övergångar
    @Transactional
    public Booking confirm(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        if (booking.getStatus() == BookingStatus.CONFIRMED)
            throw new IllegalStateException("Booking already confirmed: " + id);

        booking.setStatus(BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    private Booking getBookingByIdOrThrow(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException(id));
    }

    private Restaurant getRestaurantByIdOrThrow(Long id) {
        return restaurantRepository.findById(id)
                .orElseThrow(() -> new RestaurantNotFoundException(id));
    }
}
