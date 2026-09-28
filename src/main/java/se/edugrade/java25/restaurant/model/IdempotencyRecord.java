package se.edugrade.java25.restaurant.model;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "idempotency_record",
        uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_key", columnNames = "idempotency_key"))
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(nullable = false)
    private String payloadHash;

    private int responseStatus;

    private String responseLocation;

    @Lob //Large Object
    private String responseBody;

    private Instant createdAt;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(String idempotencyKey, String payloadHash) {
        this.idempotencyKey = idempotencyKey;
        this.payloadHash = payloadHash;
        this.createdAt = Instant.now();
    }

    public void IdempotencyRecordResponse(int status, String location, String body) {
        this.responseStatus = status;
        this.responseLocation = location;
        this.responseBody = body;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getPayloadHash() {
        return payloadHash;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public String getResponseLocation() {
        return responseLocation;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
