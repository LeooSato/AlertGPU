package com.sato.alertsgpu.api.dto;

import com.sato.alertsgpu.core.domain.Store;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateAlertRequest(
    @NotBlank String name,
    String gpuFamily,
    @NotNull Integer gpuModel,
    @NotNull Integer vramGb,
    @NotNull @DecimalMin("0.01") BigDecimal priceMin,
    @NotNull @DecimalMax("999999.99") BigDecimal priceMax,
    @NotEmpty Set<Store> stores,
    Boolean enabled
) {
}
