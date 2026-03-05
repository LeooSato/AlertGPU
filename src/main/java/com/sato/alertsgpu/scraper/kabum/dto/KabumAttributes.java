package com.sato.alertsgpu.scraper.kabum.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KabumAttributes(
        String title,
        BigDecimal price,
        @JsonProperty("price_with_discount") BigDecimal priceWithDiscount
) {}
