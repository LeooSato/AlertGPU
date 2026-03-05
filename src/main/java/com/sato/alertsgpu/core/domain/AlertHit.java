package com.sato.alertsgpu.core.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "alert_hits",
        uniqueConstraints = {
                // evita notificar 2x o mesmo alerta pro mesmo produto
                @UniqueConstraint(name = "uk_alert_hit_alert_listing", columnNames = {"alert_id", "listing_id"})
        },
        indexes = {
                @Index(name = "idx_alert_hits_alert", columnList = "alert_id"),
                @Index(name = "idx_alert_hits_hitAt", columnList = "hitAt")
        }
)
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertHit {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_id", nullable = false)
    private Alert alert;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "listing_id", nullable = false)
    private Listing listing;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal hitPrice;

    @Builder.Default
    @Column(nullable = false)
    private Boolean notified = false;

    @Column(nullable = false)
    private Instant hitAt;

    @PrePersist
    void prePersist() {
        if (hitAt == null) hitAt = Instant.now();
        if (notified == null) notified = false;
        if (hitPrice == null && listing != null) hitPrice = listing.getPrice();
    }
}