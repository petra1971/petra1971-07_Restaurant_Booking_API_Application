package se.edugrade.java25.restaurant.exception;

public class NotificationException extends RuntimeException {

    public NotificationException(Long id) {
        super(" " + id + " not found");
    }

    public NotificationException(Long id, Throwable cause) {
        super("Restaurant " + id + " not found", cause);
    }
}
