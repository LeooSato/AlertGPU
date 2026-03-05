package com.sato.alertsgpu.scraper;

import com.sato.alertsgpu.model.Alert;
import com.sato.alertsgpu.model.Store;

import java.util.List;

public interface StoreScraper {

    Store store();

    List<ScrapedItem> search(Alert alert);

}