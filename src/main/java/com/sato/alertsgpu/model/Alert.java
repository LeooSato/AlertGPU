package com.sato.alertsgpu.model;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue
    private UUID id;

    private String name;

    private String gpuFamily; // RTX, RX

    private Integer gpuModel; // 5070

    private Integer vramGb; // 12

    private BigDecimal priceMin;

    private BigDecimal priceMax;

    private Boolean enabled = true;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<Store> stores;


    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    void prePersist() {
        this.createdAt = Instant.now();
        if (this.gpuFamily == null || this.gpuFamily.isBlank()) this.gpuFamily = "RTX";
        if (this.enabled == null) this.enabled = true;
    }
}