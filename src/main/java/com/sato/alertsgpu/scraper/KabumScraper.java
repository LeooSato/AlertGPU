package com.sato.alertsgpu.scraper;

import com.sato.alertsgpu.core.domain.Alert;
import com.sato.alertsgpu.core.domain.Store;
import com.sato.alertsgpu.scraper.kabum.dto.KabumCatalogResponse;
import com.sato.alertsgpu.scraper.kabum.dto.KabumProduct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Slf4j
@Component
@RequiredArgsConstructor
public class KabumScraper implements StoreScraper {

    private final WebClient.Builder webClientBuilder;

    @Value("${alerts.kabum.base-url}")
    private String baseUrl;

    @Override
    public Store store() {
        return Store.KABUM;
    }

    @Override
    public List<ScrapedItem> search(Alert alert) {
        log.info("Kabum baseUrl = {}", baseUrl);
        log.debug("Starting Kabum search for alert: {} - GPU: {} {}",
                alert.getName(),
                alert.getGpuFamily(),
                alert.getGpuModel());

        WebClient client = webClientBuilder
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer ->
                                configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                        .build())
                .build();

        List<ScrapedItem> results = new ArrayList<>();

        int pageSize = 20;
        int maxPages = 30; // pode existir muita página mesmo

        for (int page = 1; page <= maxPages; page++) {
            String url = buildGpuUrl(page, pageSize);
            log.info("Calling Kabum URL: {}", url);

            KabumCatalogResponse response;
            try {
                response = client.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(KabumCatalogResponse.class)
                        .block();
            } catch (Exception e) {
                log.error("Error calling Kabum API on page {}: {}", page, e.getMessage(), e);
                break;
            }

            if (response == null || response.data() == null || response.data().isEmpty()) {
                log.info("Kabum returned no data on page {}", page);
                break;
            }

            List<ScrapedItem> pageItems = mapAndFilter(response, alert);
            log.info("Kabum page {} returned {} matching items", page, pageItems.size());

            results.addAll(pageItems);

            // se já encontrou algo, pode parar
            if (!results.isEmpty()) {
                break;
            }
        }

        log.info("Kabum search completed. gpu={} {} {}GB results={}",
                alert.getGpuFamily(),
                alert.getGpuModel(),
                alert.getVramGb(),
                results.size());

        return results;
    }

    private String buildGpuUrl(int pageNumber, int pageSize) {
        return UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/catalog/v2/products-by-category/hardware/placa-de-video-vga/placa-de-video-nvidia")
                .queryParam("page_number", pageNumber)
                .queryParam("page_size", pageSize)
                .queryParam("sort", "most_searched")
                .build(true)
                .toUriString();
    }

    private List<ScrapedItem> mapAndFilter(KabumCatalogResponse response, Alert alert) {
        List<ScrapedItem> items = new ArrayList<>();

        for (KabumProduct product : response.data()) {
            if (product == null || product.attributes() == null) {
                continue;
            }

            String title = product.attributes().title();
            if (title == null || title.isBlank()) {
                continue;
            }

            if (!matchesAlert(title, alert)) {
                continue;
            }

            BigDecimal price = product.attributes().priceWithDiscount() != null
                    ? product.attributes().priceWithDiscount()
                    : product.attributes().price();

            if (price == null) {
                continue;
            }

            Boolean available = product.attributes().available();
            Integer stock = product.attributes().stock();

            if (Boolean.FALSE.equals(available)) {
                continue;
            }

            if (stock != null && stock <= 0) {
                continue;
            }

            String productUrl = buildProductUrl(product);

            log.info("Matched Kabum product: title='{}', price={}, url={}", title, price, productUrl);

            items.add(new ScrapedItem(
                    Store.KABUM,
                    title,
                    price,
                    productUrl
            ));
        }

        return items;
    }

    private boolean matchesAlert(String title, Alert alert) {
        String normalized = normalize(title);

        boolean familyMatch = normalized.contains(normalize(alert.getGpuFamily()));
        boolean modelMatch = normalized.contains(String.valueOf(alert.getGpuModel()));
        boolean vramMatch = normalized.contains(alert.getVramGb() + "gb")
                || normalized.contains(alert.getVramGb() + " gb")
                || normalized.contains(alert.getVramGb() + "g")
                || normalized.contains(alert.getVramGb() + " g");

        return familyMatch && modelMatch && vramMatch;
    }

    private String buildProductUrl(KabumProduct product) {
        if (product.attributes() != null
                && product.attributes().productLink() != null
                && !product.attributes().productLink().isBlank()) {
            return "https://www.kabum.com.br/produto/" + product.id() + "/" + product.attributes().productLink();
        }

        return "https://www.kabum.com.br/produto/" + product.id();
    }

    private String normalize(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).trim();
    }
}