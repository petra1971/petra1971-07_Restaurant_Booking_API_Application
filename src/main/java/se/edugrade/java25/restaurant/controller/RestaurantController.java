package se.edugrade.java25.restaurant.controller;

import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import se.edugrade.java25.restaurant.dto.*;
import se.edugrade.java25.restaurant.service.IdempotencyService;
import se.edugrade.java25.restaurant.service.RestaurantService;

import java.net.URI;
import java.util.Optional;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {

    private static final String JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String XML = MediaType.APPLICATION_ATOM_XML_VALUE;

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
    public ResponseEntity<OffsetRestaurantPage> allRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        return ResponseEntity.ok(restaurantService.findAll(page, size));
    }

    @GetMapping(path = "/{id}", produces = {JSON, XML})
    public RestaurantResource restaurantsById(@PathVariable @Valid Long id) {
        return restaurantService.findById(id);
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
                .replaceQuery(null)                 //Tar bort eventuella query-parametrar.
                .path("/{id}")                      //Utgår från URL:en för den aktuella HTTP-requesten och lägger till /{id}
                .buildAndExpand(saved.id())         //Ersätter {id} med ID:t från det sparade objektet
                .toUri();                           //Gör den färdiga URL:en till ett Java URI-objekt
        return ResponseEntity.created(location).body(saved); // Returnerar ett HTTP-svar med: HTTP/1.1 201 Created, location: /api/tasks/42 samt body
    }
}
