package se.edugrade.java25.restaurant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import se.edugrade.java25.restaurant.model.IdempotencyRecord;

import java.util.Optional;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, Long> {

    Optional<IdempotencyRecord> findByIdempotencyKey(String idempotencyKey);
}
