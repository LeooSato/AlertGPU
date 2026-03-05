package com.sato.alertsgpu.scheduler;

import com.sato.alertsgpu.core.service.MatchService;
import com.sato.alertsgpu.core.domain.*;
import com.sato.alertsgpu.integration.telegram.TelegramNotifier;
import com.sato.alertsgpu.core.repository.AlertHitRepository;
import com.sato.alertsgpu.core.repository.AlertRepository;
import com.sato.alertsgpu.core.repository.ListingRepository;
import com.sato.alertsgpu.scraper.ScrapedItem;
import com.sato.alertsgpu.scraper.StoreScraper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AlertScheduler {

    private final AlertRepository alertRepository;
    private final ListingRepository listingRepository;
    private final AlertHitRepository alertHitRepository;
    private final List<StoreScraper> scrapers; // Spring injeta todos
    private final MatchService matchService;
    private final TelegramNotifier telegramNotifier;

    @Value("${alerts.scheduler.enabled:true}")
    private boolean enabled;

    @Scheduled(fixedDelayString = "${alerts.scheduler.fixedDelayMs:300000}")
    public void run() {
        if (!enabled) {
            log.info("Alert scheduler is disabled");
            return;
        }
        log.info("Starting alert scheduler run...");
        List<Alert> alerts = alertRepository.findByEnabledTrue();
        log.info("Found {} enabled alerts and {} scrapers", alerts.size(), scrapers.size());

        for (Alert alert : alerts) {
            log.info("[SCHED] alert={} stores={}", alert.getId(), alert.getStores());
            log.debug("Processing alert: {} with stores: {}", alert.getName(), alert.getStores());
            for (Store store : alert.getStores()) {
                log.debug("Looking for scraper for store: {}", store);
                StoreScraper scraper = scrapers.stream()
                        .filter(s -> s.store() == store)
                        .findFirst()
                        .orElse(null);

                log.info("[SCHED] store={} scraper={}", store, scraper == null ? "NULL" : scraper.getClass().getSimpleName());

                if (scraper == null) {
                    log.warn("No scraper found for store: {}", store);
                    continue;
                }

                log.debug("Executing scraper for store: {}", store);
                List<ScrapedItem> items = scraper.search(alert);
                log.info("[SCHED] store={} items={}", store, items.size());
                log.debug("Scraper returned {} items for alert: {}", items.size(), alert.getName());

                for (ScrapedItem item : items) {
                    log.debug("Checking item: {} - Price: {}", item.title(), item.price());

                    if (!matchService.matches(alert, item.title())) {
                        log.debug("Item does not match alert criteria: {}", item.title());
                        continue;
                    }

                    if (!withinRange(item.price(), alert.getPriceMin(), alert.getPriceMax())) {
                        log.debug("Item price {} is out of range [{}, {}]", item.price(), alert.getPriceMin(), alert.getPriceMax());
                        continue;
                    }

                    String fingerprint = sha256(item.store() + "|" + item.url());

                    Listing listing = listingRepository.findByFingerprint(fingerprint)
                            .orElseGet(() -> listingRepository.save(
                                    Listing.builder()
                                            .store(item.store())
                                            .title(item.title())
                                            .price(item.price())
                                            .url(item.url())
                                            .fingerprint(fingerprint)
                                            .build()
                            ));

                    boolean alreadyHit = alertHitRepository.findByAlertAndListing(alert, listing).isPresent();
                    if (alreadyHit) {
                        log.debug("Alert already hit for this listing: {}", listing.getUrl());
                        continue;
                    }

                    alertHitRepository.save(AlertHit.builder()
                            .alert(alert)
                            .listing(listing)
                            .hitPrice(listing.getPrice())
                            .notified(true)
                            .build());
                    log.info("🔔 ALERT TRIGGERED: {} - {} - R$ {}", alert.getName(), item.title(), item.price());
                    telegramNotifier.send(formatMsg(alert, listing));
                }
            }
        }
        log.info("Alert scheduler run completed");
    }

    private boolean withinRange(BigDecimal price, BigDecimal min, BigDecimal max) {
        return price != null && min != null && max != null
                && price.compareTo(min) >= 0
                && price.compareTo(max) <= 0;
    }

    private String formatMsg(Alert alert, Listing listing) {
        return """
                🔔 ALERTA GPU!
                %s
                %s %d %dGB
                Preço: R$ %s
                Loja: %s
                Link: %s
                """.formatted(
                alert.getName(),
                alert.getGpuFamily(),
                alert.getGpuModel(),
                alert.getVramGb(),
                listing.getPrice(),
                listing.getStore(),
                listing.getUrl()
        );
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}