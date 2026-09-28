package se.edugrade.java25.restaurant.exception;

public class RestaurantNotFoundException extends RuntimeException{
    public RestaurantNotFoundException(Long id) {
        super("Restaurant " + id + " not found");
    }
}
