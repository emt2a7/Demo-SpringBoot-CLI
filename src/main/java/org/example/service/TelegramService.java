package org.example.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.example.dto.AiResponseWrapper;
import org.example.dto.telegram.TelegramRequest;
import org.example.framework.prop.TelegramProp;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService {

    private final TelegramProp telegramProp;
    private final RestClient.Builder restClient;
    private final AiChatService aiChatService;

    /**
     * 與 AI 進行對話，並取得回應
     * @param userPrompt
     * @return
     */
    public AiResponseWrapper<String> chat(String userPrompt) {
        // 系統提示詞
        String systemPrompt = """
        你是一個專業的 AI 助理。
        【重要規則】
        請注意你的回答內容請務必精簡，絕對不可超過 %d 個字元，以免超出系統的傳送限制。
        """.formatted(telegramProp.messageLimit().intValue());
        return aiChatService.chat(systemPrompt, userPrompt);
    }

    public void sendToTelegram(String telegramChatId, String text) {
        try {
            String url = telegramProp.apiUrl() + telegramProp.token() + telegramProp.sendUrl();

            // 超過 4000 字時，自動在結尾補上 "..."，且總長度保證 <= 4000
            String safeText = StringUtils.abbreviate(text, telegramProp.messageLimit().intValue());

            TelegramRequest requestBody = TelegramRequest.builder()
                    .chatId(telegramChatId)
                    .text(safeText)
                    .build();
            log.info("url:{}", url);

            // 使用 RestClient 發送請求
            String reesponse = restClient.build().post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    // 注意：這裡我用最簡單的字串拼接示範。建議實務上可改用 DTO 物件 (如上一篇提到的 TelegramMessageRequest)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            log.info("✅ 成功傳送至 Telegram！");
        } catch (Exception e) {
            log.error("❌ 傳送至 Telegram 失敗！", e);
        }
    }
}
