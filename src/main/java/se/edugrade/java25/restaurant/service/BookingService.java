package se.edugrade.java25.restaurant.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import se.edugrade.java25.restaurant.dto.BookingResource;
import se.edugrade.java25.restaurant.dto.CreateBookingRequest;
import se.edugrade.java25.restaurant.dto.OffsetPage;
import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.model.Restaurant;
import se.edugrade.java25.restaurant.repository.BookingRepository;
import se.edugrade.java25.restaurant.repository.RestaurantRepository;

// Business logic lives here, not in the controller
@Service
public class BookingService {

    private final BookingRepository bookings;
    private final RestaurantRepository restaurants;

    private static final int MAX_PAGE_SIZE = 100;

    public BookingService(BookingRepository bookings, RestaurantRepository restaurants) {
        this.bookings = bookings;
        this.restaurants = restaurants;
    }

    @Transactional
    public BookingResource createBooking(CreateBookingRequest request) {
        Restaurant restaurant = getRestaurantByIdOrThrow(request.restaurantId());
        Booking booking = CreateBookingRequest.toBooking(request, restaurant);
        Booking saved = bookings.save(booking);
        return BookingResource.from(saved);
    }

    public OffsetPage findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, clamp(size));
        Page<Booking> result = bookings.findAll(pageable);

        return new OffsetPage(
                result.getContent()
                        .stream()
                        .map(BookingResource::from)
                        .toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    //Ta requested men se till att värdet inte kan bli mindre än ett minsta värde eller större än ett största värde.
    private static int clamp(int requested) {
        return  Math.min(Math.max(requested, 1), MAX_PAGE_SIZE);
    }

    public BookingResource findById(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        return BookingResource.from(booking);
    }

    @Transactional
    public BookingResource confirm(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        booking.confirm();
        return BookingResource.from(bookings.save(booking));
    }

    @Transactional
    public BookingResource seat(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        booking.seat();
        return BookingResource.from(bookings.save(booking));
    }

    @Transactional
    public BookingResource cancel(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        booking.cancel();
        return BookingResource.from(bookings.save(booking));
    }

    private Booking getBookingByIdOrThrow(Long id) {
        return bookings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No booking found with id " + id));
    }

    private Restaurant getRestaurantByIdOrThrow(Long id) {
        return restaurants.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No restaurant found with id " + id));
    }
}
