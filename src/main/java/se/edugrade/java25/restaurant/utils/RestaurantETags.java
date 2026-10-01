package se.edugrade.java25.restaurant.utils;

import se.edugrade.java25.restaurant.model.Restaurant;

/**
 * One place decides what a task's ETag looks like.
 *
 * <p>The tag is built from the task's own revision counter, not from a hash of
 * the serialized body. That is a deliberate choice, and it is what lets step 5
 * add HATEOAS links to every response without invalidating a single cached
 * copy: the representation gains links, the resource's state does not
 * change.</p>
 *
 * <p>The quotes are part of the value. {@code ETag: task-1-r3} is not a
 * malformed header - it is a different header, and a strict comparison against
 * a quoted tag will never match it.</p>
 *
 * <p>Opaque means the client compares it and never parses it. The moment a
 * client starts reading the revision out of your tag, you can never change how
 * you generate it.</p>
 */
public final class RestaurantETags {

    private RestaurantETags() {
    }

    public static String of(Restaurant restaurant) {
        return "\"restaurant-" + restaurant.getId() + "-r" + restaurant.getRevision() + "\"";
    }
}
