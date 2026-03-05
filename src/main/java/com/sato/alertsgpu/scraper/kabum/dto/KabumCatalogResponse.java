// KabumCatalogResponse.java
package com.sato.alertsgpu.scraper.kabum.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record KabumCatalogResponse(
        List<KabumItem> data,
        KabumMeta meta
) {}

