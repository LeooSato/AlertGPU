package com.sato.alertsgpu.scraper;

import com.sato.alertsgpu.model.Store;

import java.math.BigDecimal;

public record ScrapedItem(
        Store store,
        String title,
        BigDecimal price,
        String url
) {}