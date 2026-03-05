package com.sato.alertsgpu.model.dto;

import com.sato.alertsgpu.model.Store;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.Set;

public record CreateAlertRequest(
        @NotBlank @Size(max = 120) String name,

        // Se quiser travar em "RTX" no MVP, dá pra remover do request e setar fixo no back
        @Size(max = 16) String gpuFamily,

        @NotNull @Min(1000) Integer gpuModel,
        @NotNull @Min(1) Integer vramGb,

        @NotNull @DecimalMin("0.00") BigDecimal priceMin,
        @NotNull @DecimalMin("0.00") BigDecimal priceMax,

        @NotEmpty Set<Store> stores
) {}