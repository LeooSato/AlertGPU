package com.sato.alertsgpu.scraper.kabum.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KabumItem(
        String id,
        KabumAttributes attributes
) {}
