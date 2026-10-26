package se.edugrade.java25.restaurant.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;
// Klassen servar Resaurant-Controllern med idenpotency
@Service
public class IdempotencyService {

    private final IdempotencyGuard idempotency;

    public IdempotencyService(IdempotencyGuard idempotency) {
        this.idempotency = idempotency;
    }

    //Se BookingController för att se koden där inget brutits ut. Förklaringar finns där.
    public ResponseEntity<Object> createWithIdempotency(
            String idempotencyKey,
            Object body,
            Supplier<ResponseEntity<Object>> createAction) { // Supplier<T> representerar (alltid) ett funktionellt interface som kan producera eller leverera ett värde av typen T.

        if (idempotencyKey == null) {
            return createAction.get();
        }

        Optional<ResponseEntity<Object>> replay =
                idempotency.claim(
                        idempotencyKey, IdempotencyGuard.hash(body)
                );
        if (replay.isPresent()) {
            return replay.get();
        }

        ResponseEntity<Object> created = createAction.get();
        idempotency.storeResponse(
                idempotencyKey,
                created.getStatusCode().value(),
                created.getHeaders().getFirst("Location"),
                created.getBody()
        );
        return created;
    }
}
