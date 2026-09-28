package se.edugrade.java25.restaurant.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.edugrade.java25.restaurant.dto.CreateBookingRequest;
import se.edugrade.java25.restaurant.dto.BookingResource;
import se.edugrade.java25.restaurant.dto.OffsetBookingPage;
import se.edugrade.java25.restaurant.service.BookingService;
import se.edugrade.java25.restaurant.service.IdempotencyService;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String XML = MediaType.APPLICATION_ATOM_XML_VALUE;

    private final BookingService bookingService;
    private final IdempotencyService idempotencyService;

    public BookingController(BookingService bookingService,
                             IdempotencyService idempotencyService) {
        this.bookingService = bookingService;
        this.idempotencyService = idempotencyService;
    }

    /**
     * Offset pagination:
     * page =  sida, size = hur många poster per sida
     */
    @GetMapping(produces = {JSON, XML})
    public ResponseEntity<OffsetBookingPage> allBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(bookingService.findAll(page, size));
    }

    @GetMapping(path = "/{id}", produces = {JSON, XML})
    public BookingResource bookingsById(@PathVariable @Valid Long id) {
        return bookingService.findById(id);
    }

    /**
     * {@code Idempotency-Key}
     * The header is optional here: a client that does not send one gets the
     * plain POST semantics it asked for, duplicates included.
     */
    @PostMapping(consumes = JSON, produces = {JSON, XML})
    public ResponseEntity<Object> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody @Valid CreateBookingRequest body) {

        // Pga DRY har jag brutit ut koden för create till en idempotencyService eftersom den fanns i båda entiteterna
        return idempotencyService.createWithIdempotency(
                idempotencyKey,
                body,
                () -> doCreate(body)
        );
    }

    private ResponseEntity<Object> doCreate(CreateBookingRequest body) {
        BookingResource saved = bookingService.create(body);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQuery(null)                 //Tar bort eventuella query-parametrar.
                .path("/{id}")                      //Utgår från URL:en för den aktuella HTTP-requesten och lägger till /{id}
                .buildAndExpand(saved.id())         //Ersätter {id} med ID:t från det sparade objektet
                .toUri();                           //Gör den färdiga URL:en till ett Java URI-objekt
        return ResponseEntity.created(location).body(saved); // Returnerar ett HTTP-svar med: HTTP/1.1 201 Created, location: /api/tasks/42 samt body
    }

    @PostMapping(path = "/{id}/confirm", produces = {JSON, XML})
    public BookingResource confirm(@PathVariable Long id) {
        return bookingService.confirm(id);
    }

    @PostMapping(path = "/{id}/seat", produces = {JSON, XML})
    public BookingResource seat(@PathVariable Long id) {
        return bookingService.seat(id);
    }

    @PostMapping(path = "/{id}/cancel", produces = {JSON, XML})
    public BookingResource cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }
}
