package com.sato.alertsgpu.api.dto;

import com.sato.alertsgpu.core.domain.Store;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record AlertResponse(
    UUID id,
    String name,
    String gpuFamily,
    Integer gpuModel,
    Integer vramGb,
    BigDecimal priceMin,
    BigDecimal priceMax,
    Boolean enabled,
    Set<Store> stores,
    Instant createdAt
) {
}
