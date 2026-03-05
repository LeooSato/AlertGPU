package com.sato.alertsgpu.integration.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class TelegramNotifier {

    private final RestClient restClient = RestClient.create();

    @Value("${alerts.telegram.enabled:false}")
    private boolean enabled;

    @Value("${alerts.telegram.bot-token:}")
    private String botToken;

    @Value("${alerts.telegram.chat-id:}")
    private String chatId;

    public void send(String msg) {
        if (!enabled) return;
        if (botToken == null || botToken.isBlank()) return;
        if (chatId == null || chatId.isBlank()) return;

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Payload(chatId, msg))
                .retrieve()
                .toBodilessEntity();
    }

    private record Payload(String chat_id, String text) {}
}