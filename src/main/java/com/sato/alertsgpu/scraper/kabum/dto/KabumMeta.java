package com.sato.alertsgpu.scraper.kabum.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KabumMeta(
        @JsonProperty("total_pages_count") int totalPagesCount
) {}
