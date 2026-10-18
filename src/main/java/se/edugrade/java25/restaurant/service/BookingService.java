package se.edugrade.java25.restaurant.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.edugrade.java25.restaurant.notification.NotificationClient;
import se.edugrade.java25.restaurant.dto.CreateBookingRequest;
import se.edugrade.java25.restaurant.exception.BookingNotFoundException;
import se.edugrade.java25.restaurant.exception.RestaurantNotFoundException;
import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.model.Restaurant;
import se.edugrade.java25.restaurant.repository.BookingRepository;
import se.edugrade.java25.restaurant.repository.RestaurantRepository;

// Business logic lives here, not in the controller
@Service
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final RestaurantRepository restaurantRepository;
    private final NotificationClient notificationClient;

    private static final int MAX_PAGE_SIZE = 100;

    public BookingService(BookingRepository bookings, RestaurantRepository restaurants, NotificationClient notificationClient) {
        this.bookingRepository = bookings;
        this.restaurantRepository = restaurants;
        this.notificationClient = notificationClient;
    }

    @Transactional
    public Booking create(CreateBookingRequest request) {
        Restaurant restaurant = getRestaurantByIdOrThrow(request.restaurantId());
        Booking booking = CreateBookingRequest.toBooking(request, restaurant);
        return bookingRepository.save(booking);
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
    public Booking confirm(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        booking.confirm();

        // Mitt API anropar här en extern aviseringstjänst när en bokning bekräftas. Den tjänsten finns inte i verkligheten
        // Här skulle finnas try catch om externa tjänsten verkligen fanns
        if (notificationClient.sendConfirmation(id)) {
            booking.setConfirmationSent();
        }

        //Den som anropar API:t får själv hantera att bokningens reminderSent är false om
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking seat(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        booking.seat();
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking cancel(Long id) {
        Booking booking = getBookingByIdOrThrow(id);
        booking.cancel();
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
