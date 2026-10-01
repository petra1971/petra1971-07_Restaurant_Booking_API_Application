package se.edugrade.java25.restaurant.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.function.Supplier;

@Service
public class IdempotencyService {

    private final IdempotencyGuard idempotency;

    public IdempotencyService(IdempotencyGuard idempotency) {
        this.idempotency = idempotency;
    }


    public ResponseEntity<Object> createWithIdempotency(
            String idempotencyKey,
            Object body,
            Supplier<ResponseEntity<Object>> createAction) { // createAction är något som kan ge oss ett ResponseEntity<Object> när vi ber om det.

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
        idempotency.store(
                idempotencyKey,
                created.getStatusCode().value(),
                created.getHeaders().getFirst("Location"),
                created.getBody()
        );
        return created;
    }
}
