package com.sato.alertsgpu.integration.kabum;

import com.sato.alertsgpu.integration.kabum.dto.KabumResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class KabumClient {

    private final RestClient restClient;
    private final String baseUrl;

    public KabumClient(@Value("${alerts.kabum.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().build();
        this.baseUrl = baseUrl;
    }


    public KabumResponse fetchHardwarePage(int pageNumber, int pageSize) {
        String url = UriComponentsBuilder
                .fromUriString(baseUrl)
                .path("/catalog/v2/products-by-category/hardware")
                .queryParam("page_number", pageNumber)
                .queryParam("page_size", pageSize)
                .queryParam("facet_filters", "")
                .queryParam("sort", "most_searched")
                .queryParam("is_prime", "false")
                .queryParam("payload_data", "products_category_filters")
                .queryParam("include", "gift")
                .toUriString();

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(KabumResponse.class);
    }
}