package se.edugrade.java25.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.edugrade.java25.restaurant.entity.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

}
