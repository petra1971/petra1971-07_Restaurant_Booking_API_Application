package se.edugrade.java25.restaurant.exception;

import org.springframework.http.HttpStatus;

/** 422: the request was understood, and it contradicts a request we already honoured. */
public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String key) {
        super("Idempotency-Key '" + key + "' was already used with a different request body");
    }
    public HttpStatus status() {
        return HttpStatus.UNPROCESSABLE_ENTITY;
    }
}
