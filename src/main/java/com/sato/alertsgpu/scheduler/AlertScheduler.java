package com.sato.alertsgpu.scheduler;

import com.sato.alertsgpu.match.MatchService;
import com.sato.alertsgpu.model.*;
import com.sato.alertsgpu.notify.TelegramNotifier;
import com.sato.alertsgpu.repository.AlertHitRepository;
import com.sato.alertsgpu.repository.AlertRepository;
import com.sato.alertsgpu.repository.ListingRepository;
import com.sato.alertsgpu.scraper.ScrapedItem;
import com.sato.alertsgpu.scraper.StoreScraper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.math.BigDecimal;
import java.util.HexFormat;
import java.util.List;

@Component
@RequiredArgsConstructor
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
        if (!enabled) return;
        System.out.println("[SCHED] rodando...");
        List<Alert> alerts = alertRepository.findByEnabledTrue();
        System.out.println("[SCHED] alerts enabled=" + alerts.size() + " | scrapers=" + scrapers.size());

        for (Alert alert : alerts) {
            for (Store store : alert.getStores()) {
                StoreScraper scraper = scrapers.stream()
                        .filter(s -> s.store() == store)
                        .findFirst()
                        .orElse(null);

                if (scraper == null) continue;

                List<ScrapedItem> items = scraper.search(alert);

                for (ScrapedItem item : items) {
                    if (!matchService.matches(alert, item.title())) continue;

                    if (!withinRange(item.price(), alert.getPriceMin(), alert.getPriceMax())) continue;

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
                    if (alreadyHit) continue;

                    alertHitRepository.save(AlertHit.builder()
                            .alert(alert)
                            .listing(listing)
                            .hitPrice(listing.getPrice())
                            .notified(true)
                            .build());
                    System.out.println("[SCHED] alerts=" + alerts.size());
                    telegramNotifier.send(formatMsg(alert, listing));
                }
            }
        }
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