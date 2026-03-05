package com.sato.alertsgpu.scraper;

import com.sato.alertsgpu.core.domain.Store;

import java.math.BigDecimal;

public record ScrapedItem(
        Store store,
        String title,
        BigDecimal price,
        String url
) {}