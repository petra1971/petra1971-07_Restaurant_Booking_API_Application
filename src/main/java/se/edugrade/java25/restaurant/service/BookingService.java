package se.edugrade.java25.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.edugrade.java25.restaurant.dto.BookingResource;
import se.edugrade.java25.restaurant.dto.CreateBookingRequest;
import se.edugrade.java25.restaurant.dto.OffsetBookingPage;
import se.edugrade.java25.restaurant.exception.BookingNotFoundException;
import se.edugrade.java25.restaurant.exception.RestaurantNotFoundException;
import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.model.Restaurant;
import se.edugrade.java25.restaurant.repository.BookingRepository;
import se.edugrade.java25.restaurant.repository.RestaurantRepository;

// Business logic lives here, not in the controller
@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;

    private static final int MAX_PAGE_SIZE = 100;

    public BookingService(BookingRepository bookings, RestaurantRepository restaurants) {
        this.bookingRepository = bookings;
        this.restaurantRepository = restaurants;
    }

    @Transactional
    public BookingResource create(CreateBookingRequest request) {
        Restaurant restaurant = getRestaurantByIdOrThrow(request.restaurantId());
        Booking booking = CreateBookingRequest.toBooking(request, restaurant);
        Booking saved = bookingRepository.save(booking);
        return BookingResource.from(saved);
    }

    public Page<Booking> findAll(Pageable pageable) {
        return bookingRepository.findAll(pageable);
    }

    //Ta requested men se till att värdet inte kan bli mindre än ett minsta värde eller större än ett största värde.
    private static int clamp(int requested) {
        return  Math.min(Math.max(requested, 1), MAX_PAGE_SIZE);
    }

    public Booking findById(Long id) {
        return getBookingByIdOrThrow(id);
    }

    @Transactional
    public BookingResource confirm(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        booking.confirm();
        return BookingResource.from(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResource seat(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        booking.seat();
        return BookingResource.from(bookingRepository.save(booking));
    }

    @Transactional
    public BookingResource cancel(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        booking.cancel();
        return BookingResource.from(bookingRepository.save(booking));
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
