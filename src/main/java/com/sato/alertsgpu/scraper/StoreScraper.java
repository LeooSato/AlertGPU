package com.sato.alertsgpu.scraper;

import com.sato.alertsgpu.core.domain.Alert;
import com.sato.alertsgpu.core.domain.Store;

import java.util.List;

public interface StoreScraper {

    Store store();

    List<ScrapedItem> search(Alert alert);

}