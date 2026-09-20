package se.edugrade.java25.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.edugrade.java25.restaurant.entity.Restaurant;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {

}
