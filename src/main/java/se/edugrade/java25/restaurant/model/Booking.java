package se.edugrade.java25.restaurant.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import se.edugrade.java25.restaurant.exception.IllegalTransitionException;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String guestName;
    private String guestEmail;
    private LocalDateTime arrivalTime;
    private int partySize;
    private Instant updatedAt;


    @ManyToOne
    @JsonIgnore // Booking - barnsidan, serialiseras INTE. Förhindrar cirkulära refrenser
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;
    private int revision;

    // JPA requires a no-arg constructor.
    protected Booking() { }

    public Booking(String guestName, String guestEmail, LocalDateTime arrivalTime, int partySize, Restaurant restaurant) {
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.arrivalTime = arrivalTime;
        this.partySize = partySize;
        this.updatedAt = Instant.now();
        this.restaurant = restaurant;
        this.status = BookingStatus.REQUESTED;
        this.revision = 1;
    }

    public Long getId() {
        return Id;
    }

    public String getGuestName() {
        return guestName;
    }

    public String getGuestEmail() {
        return guestEmail;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public int getPartySize() {
        return partySize;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public int getRevision() {
        return revision;
    }

    public void applyUpdate(String bookersName, String email, LocalDateTime arrivalTime, int seats) {
        this.guestName = bookersName;
        this.guestEmail = email;
        this.arrivalTime = arrivalTime;
        this.partySize = seats;
        updateTime();
    }

    public boolean canConfirm() {
        return status == BookingStatus.REQUESTED;
    }

    public boolean canSeat() {
        return status == BookingStatus.CONFIRMED;
    }

    public boolean canCancel() {
        return status != BookingStatus.CANCELLED;
    }

    public void confirm() {
        requireLegal(canConfirm(), "confirm");
        transitionTo(BookingStatus.CONFIRMED);
    }

    public void seat() {
        requireLegal(canSeat(), "seat");
        transitionTo(BookingStatus.SEATED);
    }

    public void cancel() {
        requireLegal(canCancel(), "cancel");
        transitionTo(BookingStatus.CANCELLED);
    }

    // legal = allowed transition. Require that transition is allowed, if not throw exception
    private void requireLegal(boolean legal, String transition) {
        if (!legal) {
            throw new IllegalTransitionException(transition, status);
        }
    }

    private void transitionTo(BookingStatus targetStatus) {
        this.status = targetStatus;
        updateTime();
    }

    private void updateTime() {
        this.updatedAt = Instant.now();
    }
}
