package se.edugrade.java25.restaurant.exception;

import se.edugrade.java25.restaurant.model.BookingStatus;

/** Thrown when a caller asks for a transition the current state forbids. */
public class IllegalTransitionException extends RuntimeException {
    public IllegalTransitionException(String transition, BookingStatus from){
        super("Cannot " + transition + " a booking in state " + from);
    }
}
