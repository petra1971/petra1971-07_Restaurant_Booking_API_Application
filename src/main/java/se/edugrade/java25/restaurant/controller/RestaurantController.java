package se.edugrade.java25.restaurant.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.edugrade.java25.restaurant.config.ApiVersioningConfig;
import se.edugrade.java25.restaurant.dto.*;
import se.edugrade.java25.restaurant.model.Restaurant;
import se.edugrade.java25.restaurant.service.IdempotencyService;
import se.edugrade.java25.restaurant.service.RestaurantService;
import se.edugrade.java25.restaurant.utils.RestaurantETags;

import java.net.URI;
import java.time.Duration;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String XML = MediaType.APPLICATION_ATOM_XML_VALUE;

    private static final int MAX_PAGE_SIZE = 100;

    /**
     * {@code public} Restaurant details are the same for everyone, so a shared cache may hold
     * one copy and serve everyone from it. {@code max-age=5} for
     * five minutes the client reuses its copy without sending a request at all.</p>
     */
    private static final CacheControl RESTAURANT_POLICY =
            CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic();

    /**
     * VARY = "Accept, X-API-Version". Headern som anges i Vary måste tas hänsyn till av cachen både när den sparar
     * resultatet och när den avgör vilken sparad variant som ska returneras i svaret.
     */
    private static final String VARY = "Accept, " + ApiVersioningConfig.VERSION_HEADER;

    private final RestaurantService restaurantService;
    private final IdempotencyService idempotencyService;

    public RestaurantController(RestaurantService restaurantService, IdempotencyService idempotencyService) {
        this.restaurantService = restaurantService;
        this.idempotencyService = idempotencyService;
    }

    /**
     * Offset pagination:
     * page =  sida, size = hur många poster per sida
     */
    @GetMapping(produces = {JSON, XML})
    public ResponseEntity<Object> allRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(offsetPage(page, clamp(size)));
    }

    private OffsetRestaurantPage offsetPage(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);             // Pageable är ett interface i Spring Data som beskriver en sidindelad förfrågan (pagination).
                                                                    // PageRequest.of() skapar ett konkret objekt som implementerar Pageable
        Page<Restaurant> result = restaurantService.findAll(pageable);

        return new OffsetRestaurantPage(
                result.getContent().stream().map(RestaurantResource::from).toList(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages());
    }

    //Ta requested men se till att värdet inte kan bli mindre än ett minsta värde eller större än ett största värde.
    private static int clamp(int requested) {
        return  Math.min(Math.max(requested, 1), MAX_PAGE_SIZE);
    }

    /**
     * Vary, CacheControl och ETag för hantering av cachen).
     * I detta fall beror cachen på API-versionen
     */
    @GetMapping(path = "/{id}", version = "1", produces = {JSON, XML})
    public ResponseEntity<RestaurantResource> oneById_V1(
            @PathVariable @Valid Long id,
            WebRequest request,
            HttpServletResponse response) {
        var restaurant = restaurantService.findById(id);
        String tag = RestaurantETags.of(restaurant);

        response.setHeader("Vary", VARY);
        response.setHeader("Cache-Control", RESTAURANT_POLICY.getHeaderValue());

        if (request.checkNotModified(tag)) {
            // Spring has already written 304 with the ETag. A 304 has no body,
            // and returning one would be ignored at best.
            return null;
        }
        return ResponseEntity.ok().eTag(tag).body(RestaurantResource.from(restaurant));
    }

    /**
     * {@code Idempotency-Key}
     * The header is optional here: a client that does not send one gets the
     * plain POST semantics it asked for, duplicates included.
     */
    @PostMapping(consumes = JSON, produces = {JSON, XML})
    public ResponseEntity<Object> create(
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody @Valid RestaurantRequest body) {

        // Pga DRY har jag brutit ut koden för create till en idempotencyService eftersom den fanns i båda entiteterna
        return idempotencyService.createWithIdempotency(
                idempotencyKey,
                body,
                () -> doCreate(body)
        );
    }

    private ResponseEntity<Object> doCreate(RestaurantRequest body) {
        RestaurantResource saved = restaurantService.create(body);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQuery(null)                         //Tar bort eventuella query-parametrar.
                .path("/{id}")                              //Utgår från URL:en för den aktuella HTTP-requesten och lägger till /{id}
                .buildAndExpand(saved.id())                 //Ersätter {id} med ID:t från det sparade objektet
                .toUri();                                   //Gör den färdiga URL:en till ett Java URI-objekt
        return ResponseEntity.created(location).body(saved); // Returnerar ett HTTP-svar med: HTTP/1.1 201 Created, location: /api/tasks/42 samt body
    }
}
