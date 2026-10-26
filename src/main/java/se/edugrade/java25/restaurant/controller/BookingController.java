package se.edugrade.java25.restaurant.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.*;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import se.edugrade.java25.restaurant.assembler.BookingModelAssembler;
import se.edugrade.java25.restaurant.dto.*;
import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.service.BookingService;
import se.edugrade.java25.restaurant.service.IdempotencyGuard;
import se.edugrade.java25.restaurant.utils.BookingETags;

import java.net.URI;
import java.time.Duration;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String XML = MediaType.APPLICATION_XML_VALUE;

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Adding spring-boot-starter-hateoas makes
     * application/json render as HAL, but it does not add the HAL media type
     * to a handler's produces list - so a client that asks for the media type
     * by name would get 406 from an endpoint that serves HAL to everyone else.
     */
    private static final String HAL = MediaTypes.HAL_JSON_VALUE;

    /**
     * {@code private} private cache. {@code max-age=3} for three minutes
     * the client reuses its copy without sending a request at all.</p>
     */
    private static final CacheControl BOOKING_POLICY =
            CacheControl.maxAge(Duration.ofMinutes(3)).cachePrivate();

    private final BookingService bookingService;
    private final IdempotencyGuard idempotency;
    private final BookingModelAssembler assembler;

    public BookingController(BookingService bookingService,
                             IdempotencyGuard idempotency, BookingModelAssembler assembler) {
        this.bookingService = bookingService;
        this.idempotency = idempotency;
        this.assembler = assembler;
    }

    /**
     * Offset pagination:
     * page =  sida, size = hur många poster per sida
     * Används istället för Cursor pagination eftersom antalet bookings inte väntas bli jättestort
     */
    @GetMapping(produces = {JSON, HAL, XML})
    public ResponseEntity<Object> allBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // Beskriver vilka bokningar jag vill hämta
        // Exempel: GET /bookings?page=2&size=5 -> klienten vill hämta sida 2 med 5 bokningar per sida
        // -> PageRequest.of(2, 5)
        Pageable pageable = PageRequest.of(page, clamp(size));

        // innehåller bokningarna som hämtats från databasen, tillsammans med information om pagineringen.
        Page<Booking> result = bookingService.findAll(pageable);

        // omvandlar resultatet till ett API-svar med HATEOAS-länkar
        return ResponseEntity.ok(assembler.toPagedModel(result));
    }

    //Ta requested men se till att värdet inte kan bli mindre än ett minsta värde eller större än ett största värde.
    private static int clamp(int requested) {
        return  Math.min(Math.max(requested, 1), MAX_PAGE_SIZE);
    }

    @GetMapping(path = "/{id}", produces = {JSON, HAL, XML})
    public ResponseEntity<EntityModel<BookingResource>> oneById(@PathVariable Long id,
                                                   WebRequest request,
                                                   HttpServletResponse response) {
        Booking booking = bookingService.findById(id);
        String tag = BookingETags.of(booking);  // ETag används för att avgöra om en cash är aktuell eller behöver förnyas

        response.setHeader("Cache-Control", BOOKING_POLICY.getHeaderValue());

        if (request.checkNotModified(tag)) { // metod i Spring som kontrollerar om klienten redan har den senaste versionen av en resurs. Används tillsammans med ETag och HTTP-headern If-None-Match för att undvika att skicka samma data i onödan.
            // Spring has already written 304 with the ETag. A 304 has no body,
            // and returning one would be ignored at best.
            return null;
        }
        return ResponseEntity.ok()
                .eTag(tag)
                .body(assembler.toModel(booking));
    }

    @PostMapping(consumes = JSON, produces = {JSON, HAL, XML})
    public ResponseEntity<Object> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody BookingRequest body) {

        // Ingen idempotency-nyckel skickades
        // Skapa bokningen direkt
        if (idempotencyKey == null) {
            return doCreate(body); // Om iK saknas, gå direkt till skapa bokning, annars gå vidare till iK-kontroll
        }

        // Om claim() kontrollerar om nyckel och hashad body redan är sparad. Om den returnerar Optional.empty(), finns inget färdigt HTTP-svar
        // Om claim() returnerar ett object, blir previous response det redan existerande HTTP-objektet/svaret
        Optional<ResponseEntity<Object>> previousResponse =
                idempotency.claim(
                        idempotencyKey,
                        IdempotencyGuard.hash(body));

        // Om claim() returnerade ett HTTP-svar, skicka tillbaka det utan att skapa en ny bokning.
        // Annars gå vidare till doCreate som skapar bokning och sparar nyckeln
        if (previousResponse.isPresent()) {
            return previousResponse.get();
        }

        // Om claim() inte returnerade svar (Optional är tom) fortsätter vi här.
        // Nu kan bokningen skapas
        ResponseEntity<Object> created = doCreate(body); //Skapar svaret

        //Reserverar/Sparar idempotencyKey och responsen i sin helhet
        idempotency.store(idempotencyKey, created.getStatusCode().value(),
                created.getHeaders().getFirst("Location"), created.getBody());

        return created;
    }

    private ResponseEntity<Object> doCreate(BookingRequest body) {

        // 1. Omvandla request till Booking och spara i databasen
        Booking booking = bookingService.create(body);

        // 2. Omvandla Booking till BookingResource med HATEOAS-länkar
        EntityModel<BookingResource> model =
                assembler.toModel(booking);

        // 3. Hämta self-länken som assembler redan har skapat
        URI location = model.getRequiredLink(IanaLinkRelations.SELF)
                .toUri();

        // 4. Returnera 201 Created med Location-header och HATEOAS-modell
        return ResponseEntity.created(location)
                .body(model);
    }

    @PostMapping(path = "/{id}/confirm", produces = {JSON, HAL, XML})
    public EntityModel<BookingResource> confirm(@PathVariable Long id) {
        Booking booking = bookingService.confirm(id);
        return assembler
                .toModel(booking);
    }

    @PostMapping(path = "/{id}/seat", produces = {JSON, HAL, XML})
    public EntityModel<BookingResource> seat(@PathVariable Long id) {
        Booking booking = bookingService.seat(id);
        return assembler
                .toModel(booking);
    }

    @PostMapping(path = "/{id}/cancel", produces = {JSON, HAL, XML})
    public EntityModel<BookingResource> cancel(@PathVariable Long id) {
        Booking booking = bookingService.cancel(id);
        return assembler
                .toModel(booking);
    }
}
