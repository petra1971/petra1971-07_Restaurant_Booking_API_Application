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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.edugrade.java25.restaurant.assembler.BookingAssembler;
import se.edugrade.java25.restaurant.dto.*;
import se.edugrade.java25.restaurant.model.Booking;
import se.edugrade.java25.restaurant.service.BookingService;
import se.edugrade.java25.restaurant.service.IdempotencyService;
import se.edugrade.java25.restaurant.utils.BookingETags;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String XML = MediaType.APPLICATION_ATOM_XML_VALUE;

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Adding spring-boot-starter-hateoas makes
     * application/json render as HAL, but it does not add the HAL media type
     * to a handler's produces list - so a client that asks for the media type
     * by name would get 406 from an endpoint that serves HAL to everyone else.
     */
    private static final String HAL = MediaTypes.HAL_JSON_VALUE;

    /**
     * {@code private} private cache. {@code max-age=3} for five minutes
     * the client reuses its copy without sending a request at all.</p>
     */
    private static final CacheControl BOOKING_POLICY =
            CacheControl.maxAge(Duration.ofMinutes(3)).cachePrivate();

    private final BookingService bookingService;
    private final IdempotencyService idempotencyService;
    private final BookingAssembler assembler;

    public BookingController(BookingService bookingService,
                             IdempotencyService idempotencyService, BookingAssembler assembler) {
        this.bookingService = bookingService;
        this.idempotencyService = idempotencyService;
        this.assembler = assembler;
    }

    /**
     * Offset pagination:
     * page =  sida, size = hur många poster per sida
     */
    @GetMapping(produces = {JSON, HAL, XML})
    public ResponseEntity<Object> allBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(offsetPage(page, clamp(size)));
    }

    private PagedModel<EntityModel<BookingResource>> offsetPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Booking> result = bookingService.findAll(pageable);

        List<EntityModel<BookingResource>> items = result.getContent().stream()
                .map(assembler::toModel)
                .toList();

        var metadata = new PagedModel.PageMetadata(
                result.getSize(),
                result.getNumber(),
                result.getTotalElements(),
                result.getTotalPages()
        );

        PagedModel<EntityModel<BookingResource>> model = PagedModel.of(items, metadata);

        model.add(pageLink(
                page, size, IanaLinkRelations.SELF));

        model.add(pageLink(
                0, size, IanaLinkRelations.FIRST));

        if (result.getTotalPages() > 0) {
            model.add(pageLink(
                    result.getTotalPages() -1, size, IanaLinkRelations.LAST));
        }

        if (result.hasPrevious()) {
            model.add(pageLink(
                    page -1, size, IanaLinkRelations.PREV));
        }

        if (result.hasNext()) {
            model.add(pageLink(
                    page + 1, size, IanaLinkRelations.NEXT));
        }

        return model;
    }

    //Ta requested men se till att värdet inte kan bli mindre än ett minsta värde eller större än ett största värde.
    private static int clamp(int requested) {
        return  Math.min(Math.max(requested, 1), MAX_PAGE_SIZE);
    }

    private Link pageLink(int page, int size, LinkRelation relation) {
        return linkTo(methodOn(BookingController.class).allBookings(page, size))
                .withRel(relation);
    }

    @GetMapping(path = "/{id}", produces = {JSON, XML})
    public ResponseEntity<BookingResource> oneById(@PathVariable @Valid Long id,
                                                   WebRequest request,
                                                   HttpServletResponse response) {
        Booking booking = bookingService.findById(id);
        String tag = BookingETags.of(booking);

        response.setHeader("Cache-Control", BOOKING_POLICY.getHeaderValue());

        if (request.checkNotModified(tag)) {
            // Spring has already written 304 with the ETag. A 304 has no body,
            // and returning one would be ignored at best.
            return null;
        }
        return ResponseEntity.ok().eTag(tag).body(BookingResource.from(booking));
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
