package se.edugrade.java25.restaurant.dto;

import java.util.List;

/**
 * Offset pagination: the client asks for "page 4, size 20".
 *
 * <p>Cheap to implement, familiar to every client, and it can jump straight to
 * page 400. What it cannot do is stay correct while the data changes: insert a
 * row that sorts early and every later row shifts down one, so the client sees
 * one row twice and never learns it happened.</p>
 *
 * <p>It also costs more the deeper you go. {@code OFFSET 20000} asks the
 * database to read and throw away twenty thousand rows before it returns
 * anything.</p>
 */
public record OffsetRestaurantPage(
        List<RestaurantResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages) {
}
