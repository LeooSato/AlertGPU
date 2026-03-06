package com.sato.alertsgpu.scraper.kabum.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KabumProduct(
        Long id,
        KabumAttributes attributes
) {}