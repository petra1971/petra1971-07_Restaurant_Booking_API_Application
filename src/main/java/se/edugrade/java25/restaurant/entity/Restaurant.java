package se.edugrade.java25.restaurant.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String cuisine;
    private String city;
    private int seats;

    // Restaurant - föräldrasidan, serialiseras normalt
    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Booking> bookings = new ArrayList<>();

    // JPA requires a no-arg constructor.
    protected Restaurant() { }

    public Restaurant(String name, String cuisine, String city, int seats) {
        this.name = name;
        this.cuisine = cuisine;
        this.city = city;
        this.seats = seats;
    }

    public void addBooking(Booking booking) {
        bookings.add(booking);
        booking.setRestaurant(this);
    }

    public void removeBooking(Booking booking) {
        bookings.remove(booking);
        booking.setRestaurant(this);
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getSeats() {
        return seats;
    }

    public void setSeats(int seats) {
        this.seats = seats;
    }
}
