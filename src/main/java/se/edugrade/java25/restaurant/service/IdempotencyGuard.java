package se.edugrade.java25.restaurant.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import se.edugrade.java25.restaurant.model.IdempotencyRecord;
import se.edugrade.java25.restaurant.exception.IdempotencyConflictException;
import se.edugrade.java25.restaurant.repository.IdempotencyRecordRepository;

import tools.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Optional;

@Component
public class IdempotencyGuard {

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    public IdempotencyGuard(IdempotencyRecordRepository idempotencyRecordRepository, ObjectMapper objectMapper) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Claims the key. Empty means "you own it, go create". Present means "this
     * is a retry, here is what you answered last time".
     */
    public Optional<ResponseEntity<Object>> claim(String key, String payloadHash) {
        try {
            // saveAndFlush, not save: we need the INSERT to reach the database
            // now, not whenever the persistence context feels like it.
            idempotencyRecordRepository.saveAndFlush(new IdempotencyRecord(key, payloadHash));
            return Optional.empty();
        } catch (DataIntegrityViolationException duplicate) {
            IdempotencyRecord existing = idempotencyRecordRepository.findByIdempotencyKey(key)
                    .orElseThrow(() -> duplicate);

            if (!existing.getPayloadHash().equals(payloadHash)) {
                throw new IdempotencyConflictException(key);
            }
            return Optional.of(replay(existing));
        }
    }

    public void store(String key, int status, String location, Object body) {
        idempotencyRecordRepository.findByIdempotencyKey(key).ifPresent(record -> {
            record.IdempotencyRecordResponse(status, location, objectMapper.writeValueAsString(body));
            idempotencyRecordRepository.save(record);
        });
    }

    private ResponseEntity<Object> replay(IdempotencyRecord record) {
        var builder = ResponseEntity.status(record.getResponseStatus())
                .contentType(MediaType.APPLICATION_JSON)
                // Not a standard header, but with this, a client
                // can tell a replay from a fresh create
                .header("Idempotent-Replayed", "true");
        if (record.getResponseLocation() != null) {
            builder.header("Location", record.getResponseLocation());
        }
        // readTree, not the raw string: a String body would be serialized as a
        // quoted JSON string, and the client would get "{\"id\":7}".
        return builder.body(objectMapper.readTree(record.getResponseBody()));
    }

    /** Deterministic over the parsed request, so whitespace changes do not count as a different payload. */
    public static String hash(Object payload) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(String.valueOf(payload).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception e) {
            throw new IllegalStateException("SHA-256 is required by the JDK", e);
        }
    }
}
