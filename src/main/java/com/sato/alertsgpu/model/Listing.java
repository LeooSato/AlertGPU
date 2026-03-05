package com.sato.alertsgpu.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "listings",
        indexes = {
                @Index(name = "idx_listings_store", columnList = "store"),
                @Index(name = "idx_listings_capturedAt", columnList = "capturedAt")
        }
)
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Listing {

    @Id
    @GeneratedValue
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Store store;

    @Column(nullable = false, length = 220)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false, length = 600)
    private String url;

    // Pra deduplicar melhor: um hash simples (store + url)
    @Column(nullable = false, length = 64)
    private String fingerprint;

    @Column(nullable = false)
    private Instant capturedAt;

    @PrePersist
    void prePersist() {
        if (capturedAt == null) capturedAt = Instant.now();
    }
}