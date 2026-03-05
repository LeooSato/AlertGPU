package com.sato.alertsgpu.scraper;


import com.sato.alertsgpu.core.domain.Alert;
import com.sato.alertsgpu.core.domain.Store;
import com.sato.alertsgpu.scraper.ScrapedItem;
import com.sato.alertsgpu.scraper.StoreScraper;
import com.sato.alertsgpu.scraper.kabum.dto.KabumCatalogResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class KabumScraper implements StoreScraper {

    private final WebClient.Builder webClientBuilder;

    @Value("${api.kabum.baseUrl}")
    private String baseUrl;

    @Override
    public Store store() {
        return Store.KABUM;
    }

    @Override
    public List<ScrapedItem> search(Alert alert) {
        // termo base do alerta
        String query = buildQuery(alert); // ex: "RTX 5070 12GB"
        log.debug("Kabum API search query='{}'", query);

        WebClient client = webClientBuilder.baseUrl(baseUrl).build();

        // pega a 1ª página pra saber total de páginas
        KabumCatalogResponse first = client.get()
                .uri(buildHardwareUri(1, 100))
                .retrieve()
                .bodyToMono(KabumCatalogResponse.class)
                .block();

        if (first == null || first.data() == null) return List.of();

        int totalPages = Math.max(1, first.meta() != null ? first.meta().totalPagesCount() : 1);

        List<ScrapedItem> out = new ArrayList<>();

        // processa a primeira página
        out.addAll(mapAndFilter(first, query));

        // processa as demais páginas (pra começar simples, sequencial)
        for (int page = 2; page <= Math.min(totalPages, 5); page++) { // limita a 5 páginas por enquanto
            KabumCatalogResponse resp = client.get()
                    .uri(buildHardwareUri(page, 100))
                    .retrieve()
                    .bodyToMono(KabumCatalogResponse.class)
                    .block();

            if (resp == null) continue;
            out.addAll(mapAndFilter(resp, query));
        }

        log.info("Kabum API search completed. query='{}' results={}", query, out.size());
        return out;
    }

    private URI buildHardwareUri(int pageNumber, int pageSize) {
        // baseado no endpoint público observado no repo :contentReference[oaicite:3]{index=3}
        return UriComponentsBuilder
                .fromPath("/catalog/v2/products-by-category/hardware")
                .queryParam("page_number", pageNumber)
                .queryParam("page_size", pageSize)
                .queryParam("facet_filters", "")
                .queryParam("sort", "most_searched")
                .queryParam("is_prime", "false")
                .queryParam("payload_data", "products_category_filters")
                .queryParam("include", "gift")
                .build(true)
                .toUri();
    }

    private List<ScrapedItem> mapAndFilter(KabumCatalogResponse resp, String query) {
        if (resp.data() == null) return List.of();

        List<ScrapedItem> list = new ArrayList<>();
        for (var item : resp.data()) {
            if (item == null || item.attributes() == null) continue;

            String title = item.attributes().title();
            if (title == null) continue;

            // filtro simples por texto (depois a gente evolui p/ matchService)
            if (!containsIgnoreCase(title, query)) continue;

            BigDecimal price = item.attributes().priceWithDiscount() != null
                    ? item.attributes().priceWithDiscount()
                    : item.attributes().price();

            if (price == null) continue;

            String url = "https://www.kabum.com.br/produto/" + item.id(); // link curto já resolve

            list.add(new ScrapedItem(Store.KABUM, title, price, url));
        }
        return list;
    }

    private String buildQuery(Alert alert) {
        // dá match melhor que só "RTX"
        return "%s %d %dGB".formatted(alert.getGpuFamily(), alert.getGpuModel(), alert.getVramGb());
    }

    private boolean containsIgnoreCase(String text, String part) {
        return text.toLowerCase().contains(part.toLowerCase());
    }
}