package com.sato.alertsgpu.notify;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class TelegramNotifier {

    private final RestClient restClient = RestClient.create();

    @Value("${alerts.notify.telegram.enabled:false}")
    private boolean enabled;

    @Value("${alerts.notify.telegram.botToken:}")
    private String botToken;

    @Value("${alerts.notify.telegram.chatId:}")
    private String chatId;

    public void send(String message) {
        if (!enabled) return;
        if (botToken == null || botToken.isBlank() || chatId == null || chatId.isBlank()) return;

        String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

        restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new SendMessage(chatId, message))
                .retrieve()
                .toBodilessEntity();
    }

    private record SendMessage(String chat_id, String text) {}
}