package com.sato.alertsgpu.api.controller;

import com.sato.alertsgpu.core.domain.Alert;
import com.sato.alertsgpu.core.domain.Store;
import com.sato.alertsgpu.scraper.ScrapedItem;
import com.sato.alertsgpu.scraper.StoreScraper;
import com.sato.alertsgpu.scheduler.AlertScheduler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/debug")
public class KabumDebugController {

    private final List<StoreScraper> scrapers;
    private final AlertScheduler alertScheduler;

    @GetMapping("/kabum")
    public List<ScrapedItem> kabum() {
        StoreScraper kabum = scrapers.stream()
                .filter(s -> s.store() == Store.KABUM)
                .findFirst()
                .orElseThrow();

        // alerta fake só pra assinar método
        Alert fake = Alert.builder()
                .gpuModel(5070)
                .vramGb(12)
                .gpuFamily("RTX")
                .build();

        return kabum.search(fake);
    }

    @PostMapping("/run-scheduler")
    public void runScheduler() {
        alertScheduler.run();
    }
}
