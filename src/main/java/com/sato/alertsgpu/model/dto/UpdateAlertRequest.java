package com.sato.alertsgpu.model.dto;

import com.sato.alertsgpu.model.Store;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public record UpdateAlertRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 16) String gpuFamily,

        @NotNull @Min(1000) Integer gpuModel,
        @NotNull @Min(1) Integer vramGb,

        @NotNull @DecimalMin("0.00") BigDecimal priceMin,
        @NotNull @DecimalMin("0.00") BigDecimal priceMax,

        @NotEmpty Set<Store> stores,

        @NotNull Boolean enabled
) {}