package se.edugrade.java25.restaurant.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String bookersName;
    private String email;
    private LocalDateTime arrivalTime;
    private int bookingSize;

    @ManyToOne
    @JsonIgnore // Booking - barnsidan, serialiseras INTE. Förhindrar cirkulära refrenser
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    // JPA requires a no-arg constructor.
    protected Booking() { }

    public Booking(String bookersName, String email, LocalDateTime arrivalTime, int bookingSize, Restaurant restaurant, BookingStatus status) {
        this.bookersName = bookersName;
        this.email = email;
        this.arrivalTime = arrivalTime;
        this.bookingSize = bookingSize;
        this.restaurant = restaurant;
        this.status = status;
    }

    public Long getId() {
        return Id;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public String getBookersName() {
        return bookersName;
    }

    public void setBookersName(String bookersName) {
        this.bookersName = bookersName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(LocalDateTime arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public int getBookingSize() {
        return bookingSize;
    }

    public void setBookingSize(int bookingSize) {
        this.bookingSize = bookingSize;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}
