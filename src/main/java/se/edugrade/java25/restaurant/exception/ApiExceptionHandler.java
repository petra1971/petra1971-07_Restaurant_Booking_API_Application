package se.edugrade.java25.restaurant.exception;

import org.springframework.beans.factory.parsing.Problem;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import se.edugrade.java25.restaurant.controller.IdempotencyGuard;

import java.net.URI;

/**
 * self-descriptive messages applied to failures with the two ways a well-formed request can still be refused.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /**
     * 409 Conflict. Request clashed with the resource's current state.
     */
    @ExceptionHandler(IllegalTransitionException.class)
    public ProblemDetail illegalTransition(IllegalTransitionException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        problem.setTitle("Illegal state transition");
        problem.setType(URI.create("https://api.bookings/problems/illegal-transition"));
        return problem;
    }

    /**
     * 422 Unprocessable Content
     */
    @ExceptionHandler(IdempotencyConflictException.class)
    public ProblemDetail idempotencyConflict(IdempotencyConflictException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(e.status(), e.getMessage());
        problem.setTitle("Idempotency key reused with a different payload");
        problem.setType(URI.create("https://api.bookings/problems/idempotency-conflict"));
        return problem;
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ProblemDetail bookingNotFound(BookingNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,e.getMessage());
        problem.setTitle("Booking not found");
        problem.setType(URI.create("https://api.bookings/problems/booking-not-found"));
        return problem;
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ProblemDetail RestaurantNotFound(RestaurantNotFoundException e) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        problem.setTitle("Restaurant not found");
        problem.setType(URI.create("https://api.bookings/problems/Restaurant-not-found"));
        return problem;
    }

//    /** 400, and the body names the parameter without naming a single column. */
//    @ExceptionHandler(SortRules.InvalidQueryParameterException.class)
//    public ProblemDetail invalidQueryParameter(SortRules.InvalidQueryParameterException e) {
//        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
//        problem.setTitle("Unsupported query parameter");
//        problem.setType(URI.create("https://api.taskflow.example/problems/invalid-query-parameter"));
//        problem.setProperty("parameter", e.getParameter());
//        return problem;
//    }
}
