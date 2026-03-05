package com.sato.alertsgpu.scraper.kabum;

import com.sato.alertsgpu.model.Alert;
import com.sato.alertsgpu.model.Store;
import com.sato.alertsgpu.scraper.ScrapedItem;
import com.sato.alertsgpu.scraper.StoreScraper;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KabumScraper implements StoreScraper {

    private static final String SEARCH_URL =
            "https://www.kabum.com.br/busca/rtx-5070-12gb";

    @Override
    public Store store() {
        return Store.KABUM;
    }

    @Override
    public List<ScrapedItem> search(Alert alert) {

        List<ScrapedItem> results = new ArrayList<>();

        try {

            Document doc = Jsoup.connect(SEARCH_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            for (Element product : doc.select("article")) {

                String title = product.select("span").text();

                String priceRaw = product.select("[class*=price]").text();

                String url = product.select("a").attr("href");

                if (title.isBlank() || priceRaw.isBlank()) continue;

                BigDecimal price = parsePrice(priceRaw);

                results.add(
                        new ScrapedItem(
                                Store.KABUM,
                                title,
                                price,
                                "https://www.kabum.com.br" + url
                        )
                );
            }

        } catch (Exception e) {
            System.out.println("Kabum scraping error: " + e.getMessage());
        }

        return results;
    }

    private BigDecimal parsePrice(String raw) {

        try {
            String normalized = raw
                    .replace("R$", "")
                    .replace(".", "")
                    .replace(",", ".")
                    .trim();

            return new BigDecimal(normalized);

        } catch (Exception e) {
            return null;
        }
    }
}