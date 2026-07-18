package com.invoicesaas.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * OPTIONAL upgrade path: if you register a Telegram bot (via @BotFather) and the
 * recipient has started a chat with it, you can push the PDF directly into their
 * Telegram chat instead of relying on a "click to share" deep link.
 *
 * Not wired into the main flow by default since it requires the recipient's
 * chat_id, which you only get once they've messaged your bot. Left here as a
 * ready-to-use building block.
 */
@Service
@RequiredArgsConstructor
public class TelegramBotService {

    @Value("${app.telegram.bot-token}")
    private String botToken;

    private final WebClient.Builder webClientBuilder;

    public void sendDocumentToChat(String chatId, String documentUrl, String caption) {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("Telegram bot token not configured");
        }

        WebClient client = webClientBuilder.baseUrl("https://api.telegram.org/bot" + botToken).build();

        client.post()
                .uri("/sendDocument")
                .bodyValue(new SendDocumentRequest(chatId, documentUrl, caption))
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private record SendDocumentRequest(String chat_id, String document, String caption) {}
}
